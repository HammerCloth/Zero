package com.zero.web;

import com.zero.service.OAuthService;
import com.zero.service.OAuthService.AuthorizeRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OAuthController {

  private final OAuthService oauthService;

  public OAuthController(OAuthService oauthService) {
    this.oauthService = oauthService;
  }

  @GetMapping("/.well-known/oauth-protected-resource")
  public Map<String, Object> protectedResource(HttpServletRequest request) {
    String issuer = origin(request);
    return Map.of(
        "resource", issuer + "/mcp",
        "authorization_servers", List.of(issuer),
        "bearer_methods_supported", List.of("header"));
  }

  @GetMapping("/.well-known/oauth-authorization-server")
  public Map<String, Object> authorizationServer(HttpServletRequest request) {
    String issuer = origin(request);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("issuer", issuer);
    out.put("authorization_endpoint", issuer + "/oauth/authorize");
    out.put("token_endpoint", issuer + "/oauth/token");
    out.put("registration_endpoint", issuer + "/oauth/register");
    out.put("revocation_endpoint", issuer + "/oauth/revoke");
    out.put("response_types_supported", List.of("code"));
    out.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
    out.put("code_challenge_methods_supported", List.of("S256", "plain"));
    out.put("token_endpoint_auth_methods_supported", List.of("none"));
    out.put("scopes_supported", List.of(OAuthService.DEFAULT_SCOPE));
    return out;
  }

  @PostMapping("/oauth/register")
  public Map<String, Object> register(@RequestBody Map<String, Object> body) {
    return oauthService.registerClient(body == null ? Map.of() : body);
  }

  @GetMapping(value = "/oauth/authorize", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> authorizePage(
      @RequestParam("response_type") String responseType,
      @RequestParam("client_id") String clientId,
      @RequestParam("redirect_uri") String redirectUri,
      @RequestParam(value = "scope", required = false) String scope,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam("code_challenge") String codeChallenge,
      @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod) {
    AuthorizeRequest req =
        new AuthorizeRequest(responseType, clientId, redirectUri, scope, state, codeChallenge, codeChallengeMethod);
    var client = oauthService.validateAuthorizeRequest(req);
    return ResponseEntity.ok(authorizeHtml(req, client.getClientName(), null));
  }

  @PostMapping(value = "/oauth/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> authorizeSubmit(
      @RequestParam("response_type") String responseType,
      @RequestParam("client_id") String clientId,
      @RequestParam("redirect_uri") String redirectUri,
      @RequestParam(value = "scope", required = false) String scope,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam("code_challenge") String codeChallenge,
      @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
      @RequestParam("username") String username,
      @RequestParam("password") String password) {
    AuthorizeRequest req =
        new AuthorizeRequest(responseType, clientId, redirectUri, scope, state, codeChallenge, codeChallengeMethod);
    oauthService.validateAuthorizeRequest(req);
    try {
      String code = oauthService.createAuthorizationCode(req, username, password);
      return redirect(oauthService.redirectWithCode(req, code));
    } catch (ResponseStatusException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
        return redirect(oauthService.redirectWithError(req, "access_denied"));
      }
      return redirect(oauthService.redirectWithError(req, "invalid_request"));
    }
  }

  @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public Map<String, Object> token(@RequestParam Map<String, String> form) {
    String grantType = form.get("grant_type");
    if ("authorization_code".equals(grantType)) {
      return oauthService.exchangeAuthorizationCode(
          form.get("code"), form.get("client_id"), form.get("redirect_uri"), form.get("code_verifier"));
    }
    if ("refresh_token".equals(grantType)) {
      return oauthService.refresh(form.get("refresh_token"), form.get("client_id"));
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
  }

  @PostMapping(value = "/oauth/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public Map<String, Boolean> revoke(@RequestParam("token") String token) {
    oauthService.revoke(token);
    return Map.of("ok", true);
  }

  private static ResponseEntity<Void> redirect(String url) {
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
  }

  private static String authorizeHtml(AuthorizeRequest req, String clientName, String error) {
    return """
        <!doctype html>
        <html lang="zh-CN">
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <title>授权 AI 客户端</title>
          <style>
            body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #f6f7f9; color: #20242a; }
            main { max-width: 420px; margin: 9vh auto; background: white; border: 1px solid #e4e7ec; border-radius: 8px; padding: 28px; box-shadow: 0 12px 30px rgba(15, 23, 42, .08); }
            h1 { margin: 0 0 8px; font-size: 22px; }
            p { margin: 0 0 22px; color: #667085; line-height: 1.6; }
            label { display: block; margin: 14px 0 6px; font-size: 13px; color: #475467; }
            input { width: 100%%; box-sizing: border-box; border: 1px solid #d0d5dd; border-radius: 6px; padding: 10px 12px; font-size: 15px; }
            button { width: 100%%; margin-top: 20px; border: 0; border-radius: 6px; padding: 11px 14px; background: #1f6feb; color: white; font-weight: 600; cursor: pointer; }
            .scope { margin-top: 12px; padding: 10px 12px; background: #f8fafc; border-radius: 6px; font-size: 13px; color: #475467; }
            .error { color: #b42318; }
          </style>
        </head>
        <body>
          <main>
            <h1>授权 AI 客户端</h1>
            <p>%s 正在请求访问你的 Project Zero MCP 数据。请使用已有账号登录授权。</p>
            %s
            <form method="post" action="/oauth/authorize">
              %s
              <label>用户名</label>
              <input name="username" autocomplete="username" required />
              <label>密码</label>
              <input name="password" type="password" autocomplete="current-password" required />
              <div class="scope">权限范围：%s</div>
              <button type="submit">登录并授权</button>
            </form>
          </main>
        </body>
        </html>
        """
        .formatted(
            escape(clientName),
            error == null ? "" : "<p class=\"error\">" + escape(error) + "</p>",
            hidden("response_type", req.responseType())
                + hidden("client_id", req.clientId())
                + hidden("redirect_uri", req.redirectUri())
                + hidden("scope", req.scope())
                + hidden("state", req.state())
                + hidden("code_challenge", req.codeChallenge())
                + hidden("code_challenge_method", req.codeChallengeMethod()),
            escape(req.scope() == null || req.scope().isBlank() ? OAuthService.DEFAULT_SCOPE : req.scope()));
  }

  private static String hidden(String name, String value) {
    return "<input type=\"hidden\" name=\"" + escape(name) + "\" value=\"" + escape(value == null ? "" : value) + "\" />";
  }

  private static String escape(String value) {
    return value == null
        ? ""
        : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  private static String origin(HttpServletRequest request) {
    String proto = headerOr(request, "X-Forwarded-Proto", request.getScheme());
    String host = headerOr(request, "X-Forwarded-Host", request.getHeader(HttpHeaders.HOST));
    return proto + "://" + host;
  }

  private static String headerOr(HttpServletRequest request, String name, String fallback) {
    String value = request.getHeader(name);
    return value == null || value.isBlank() ? fallback : value;
  }
}
