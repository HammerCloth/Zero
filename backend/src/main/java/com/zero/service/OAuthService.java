package com.zero.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.config.JwtProperties;
import com.zero.domain.OAuthAuthorizationCode;
import com.zero.domain.OAuthAuthorizationView;
import com.zero.domain.OAuthClient;
import com.zero.domain.OAuthRefreshToken;
import com.zero.domain.User;
import com.zero.mapper.OAuthMapper;
import com.zero.mapper.UserMapper;
import com.zero.security.JwtService;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OAuthService {

  public static final String DEFAULT_SCOPE = "mcp:read";

  private final OAuthMapper oauthMapper;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final ObjectMapper objectMapper;
  private final SecureRandom random = new SecureRandom();

  public OAuthService(
      OAuthMapper oauthMapper,
      UserMapper userMapper,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      JwtProperties jwtProperties,
      ObjectMapper objectMapper) {
    this.oauthMapper = oauthMapper;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public Map<String, Object> registerClient(Map<String, Object> body) {
    String name = stringValue(body.get("client_name"), "AI Agent");
    List<String> redirectUris = stringList(body.get("redirect_uris"));
    if (redirectUris.isEmpty()) {
      throw oauthError("invalid_redirect_uri", "redirect_uris is required");
    }
    for (String uri : redirectUris) {
      validateRedirectUri(uri);
    }
    OAuthClient client = new OAuthClient();
    client.setId(newId());
    client.setClientId("mcp_" + token(24));
    client.setClientName(name);
    client.setRedirectUris(writeJson(redirectUris));
    client.setGrantTypes(writeJson(List.of("authorization_code", "refresh_token")));
    client.setScopes(writeJson(List.of(DEFAULT_SCOPE)));
    oauthMapper.insertClient(client);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("client_id", client.getClientId());
    out.put("client_name", client.getClientName());
    out.put("redirect_uris", redirectUris);
    out.put("grant_types", List.of("authorization_code", "refresh_token"));
    out.put("response_types", List.of("code"));
    out.put("scope", DEFAULT_SCOPE);
    out.put("token_endpoint_auth_method", "none");
    return out;
  }

  public OAuthClient requireClient(String clientId) {
    OAuthClient client = oauthMapper.findClientByClientId(clientId);
    if (client == null) {
      throw oauthError("invalid_client", "unknown client_id");
    }
    return client;
  }

  public OAuthClient validateAuthorizeRequest(AuthorizeRequest req) {
    OAuthClient client = requireClient(req.clientId());
    if (!readList(client.getRedirectUris()).contains(req.redirectUri())) {
      throw oauthError("invalid_redirect_uri", "redirect_uri is not registered");
    }
    if (!"code".equals(req.responseType())) {
      throw oauthError("unsupported_response_type", "response_type must be code");
    }
    if (req.codeChallenge() == null || req.codeChallenge().isBlank()) {
      throw oauthError("invalid_request", "code_challenge is required");
    }
    String method =
        req.codeChallengeMethod() == null || req.codeChallengeMethod().isBlank()
            ? "plain"
            : req.codeChallengeMethod();
    if (!"S256".equals(method) && !"plain".equals(method)) {
      throw oauthError("invalid_request", "unsupported code_challenge_method");
    }
    return client;
  }

  @Transactional
  public String createAuthorizationCode(AuthorizeRequest req, String username, String password) {
    OAuthClient client = validateAuthorizeRequest(req);
    String method =
        req.codeChallengeMethod() == null || req.codeChallengeMethod().isBlank()
            ? "plain"
            : req.codeChallengeMethod();

    User u = userMapper.findByUsername(username == null ? "" : username.trim());
    if (u == null || !passwordEncoder.matches(password == null ? "" : password, u.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
    }
    if (u.isMustChangePassword()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "请先在网页端修改密码");
    }

    String code = token(32);
    OAuthAuthorizationCode row = new OAuthAuthorizationCode();
    row.setId(newId());
    row.setCodeHash(hash(code));
    row.setUserId(u.getId());
    row.setClientId(client.getClientId());
    row.setRedirectUri(req.redirectUri());
    row.setScope(scopeOrDefault(req.scope()));
    row.setCodeChallenge(req.codeChallenge());
    row.setCodeChallengeMethod(method);
    row.setExpiresAt(Instant.now().plusSeconds(300).toString());
    oauthMapper.insertAuthorizationCode(row);
    oauthMapper.touchClient(client.getClientId());
    return code;
  }

  @Transactional
  public Map<String, Object> exchangeAuthorizationCode(
      String code, String clientId, String redirectUri, String codeVerifier) {
    OAuthClient client = requireClient(clientId);
    OAuthAuthorizationCode row = oauthMapper.findCodeByHash(hash(code == null ? "" : code));
    if (row == null || !client.getClientId().equals(row.getClientId())) {
      throw oauthError("invalid_grant", "invalid authorization code");
    }
    if (row.getUsedAt() != null || Instant.parse(row.getExpiresAt()).isBefore(Instant.now())) {
      throw oauthError("invalid_grant", "authorization code expired or used");
    }
    if (!row.getRedirectUri().equals(redirectUri)) {
      throw oauthError("invalid_grant", "redirect_uri mismatch");
    }
    if (!verifyPkce(row, codeVerifier)) {
      throw oauthError("invalid_grant", "PKCE verification failed");
    }
    if (oauthMapper.markCodeUsed(row.getId()) == 0) {
      throw oauthError("invalid_grant", "authorization code already used");
    }
    User u = userMapper.findById(row.getUserId());
    if (u == null) {
      throw oauthError("invalid_grant", "user no longer exists");
    }
    oauthMapper.touchClient(client.getClientId());
    return tokenResponse(u, client.getClientId(), row.getScope());
  }

  @Transactional
  public Map<String, Object> refresh(String refreshToken, String clientId) {
    OAuthClient client = requireClient(clientId);
    OAuthRefreshToken existing = oauthMapper.findRefreshTokenByHash(hash(refreshToken == null ? "" : refreshToken));
    if (existing == null || !client.getClientId().equals(existing.getClientId())) {
      throw oauthError("invalid_grant", "invalid refresh token");
    }
    if (existing.getRevokedAt() != null || Instant.parse(existing.getExpiresAt()).isBefore(Instant.now())) {
      throw oauthError("invalid_grant", "refresh token expired or revoked");
    }
    User u = userMapper.findById(existing.getUserId());
    if (u == null) {
      throw oauthError("invalid_grant", "user no longer exists");
    }
    oauthMapper.revokeRefreshToken(existing.getId());
    oauthMapper.touchClient(client.getClientId());
    return tokenResponse(u, client.getClientId(), existing.getScope());
  }

  @Transactional
  public void revoke(String token) {
    OAuthRefreshToken existing = oauthMapper.findRefreshTokenByHash(hash(token == null ? "" : token));
    if (existing != null) {
      oauthMapper.revokeRefreshToken(existing.getId());
    }
  }

  public List<OAuthAuthorizationView> listAuthorizations(String userId) {
    return oauthMapper.listAuthorizations(userId);
  }

  public void revokeAuthorization(String userId, String id) {
    oauthMapper.revokeUserRefreshToken(userId, id);
  }

  public void revokeAllAuthorizations(String userId) {
    oauthMapper.revokeAllUserRefreshTokens(userId);
  }

  public String redirectWithCode(AuthorizeRequest req, String code) {
    StringBuilder url = new StringBuilder(req.redirectUri());
    url.append(req.redirectUri().contains("?") ? "&" : "?");
    url.append("code=").append(urlEncode(code));
    if (req.state() != null && !req.state().isBlank()) {
      url.append("&state=").append(urlEncode(req.state()));
    }
    return url.toString();
  }

  public String redirectWithError(AuthorizeRequest req, String error) {
    String target = req.redirectUri();
    if (target == null || target.isBlank()) {
      return "/login";
    }
    StringBuilder url = new StringBuilder(target);
    url.append(target.contains("?") ? "&" : "?");
    url.append("error=").append(urlEncode(error));
    if (req.state() != null && !req.state().isBlank()) {
      url.append("&state=").append(urlEncode(req.state()));
    }
    return url.toString();
  }

  public record AuthorizeRequest(
      String responseType,
      String clientId,
      String redirectUri,
      String scope,
      String state,
      String codeChallenge,
      String codeChallengeMethod) {}

  private Map<String, Object> tokenResponse(User u, String clientId, String scope) {
    String refresh = token(48);
    OAuthRefreshToken rt = new OAuthRefreshToken();
    rt.setId(newId());
    rt.setTokenHash(hash(refresh));
    rt.setUserId(u.getId());
    rt.setClientId(clientId);
    rt.setScope(scopeOrDefault(scope));
    rt.setExpiresAt(Instant.now().plusSeconds(jwtProperties.refreshTtlSeconds()).toString());
    oauthMapper.insertRefreshToken(rt);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("access_token", jwtService.generateMcpAccessToken(u.getId(), u.isAdmin(), scopeOrDefault(scope)));
    out.put("token_type", "Bearer");
    out.put("expires_in", jwtProperties.accessTtlSeconds());
    out.put("refresh_token", refresh);
    out.put("scope", scopeOrDefault(scope));
    return out;
  }

  private boolean verifyPkce(OAuthAuthorizationCode row, String verifier) {
    if (verifier == null || verifier.isBlank()) {
      return false;
    }
    if ("plain".equals(row.getCodeChallengeMethod())) {
      return row.getCodeChallenge().equals(verifier);
    }
    String expected = base64Url(sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
    return row.getCodeChallenge().equals(expected);
  }

  private static String scopeOrDefault(String scope) {
    if (scope == null || scope.isBlank()) {
      return DEFAULT_SCOPE;
    }
    return scope.trim();
  }

  private static void validateRedirectUri(String value) {
    try {
      URI uri = URI.create(value);
      String scheme = uri.getScheme();
      if (!"https".equalsIgnoreCase(scheme)
          && !"http".equalsIgnoreCase(scheme)
          && !value.startsWith("urn:ietf:wg:oauth:2.0:oob")) {
        throw oauthError("invalid_redirect_uri", "unsupported redirect_uri scheme");
      }
    } catch (IllegalArgumentException e) {
      throw oauthError("invalid_redirect_uri", "invalid redirect_uri");
    }
  }

  private List<String> stringList(Object value) {
    List<String> out = new ArrayList<>();
    if (value instanceof List<?> list) {
      for (Object item : list) {
        if (item instanceof String s && !s.isBlank()) {
          out.add(s.trim());
        }
      }
    }
    return out;
  }

  private String stringValue(Object value, String fallback) {
    if (value instanceof String s && !s.isBlank()) {
      return s.trim();
    }
    return fallback;
  }

  private String writeJson(List<String> value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<String> readList(String json) {
    try {
      return objectMapper.readValue(
          json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    } catch (Exception e) {
      return List.of();
    }
  }

  private String token(int bytes) {
    byte[] data = new byte[bytes];
    random.nextBytes(data);
    return base64Url(data);
  }

  private static String hash(String value) {
    return base64Url(sha256(value.getBytes(StandardCharsets.UTF_8)));
  }

  private static byte[] sha256(byte[] value) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(value);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static String base64Url(byte[] value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private static ResponseStatusException oauthError(String error, String description) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, error + ": " + description);
  }
}
