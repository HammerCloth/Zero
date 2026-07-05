package com.zero.security;

import com.zero.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties props;

  public JwtService(JwtProperties props) {
    this.props = props;
  }

  public String generateAccessToken(String userId, boolean isAdmin) {
    return buildToken(userId, isAdmin, "access", props.accessSecret(), props.accessTtlSeconds() * 1000);
  }

  public String generateMcpAccessToken(String userId, boolean isAdmin, String scope) {
    return buildToken(
        userId, isAdmin, "mcp_access", props.accessSecret(), props.accessTtlSeconds() * 1000, scope);
  }

  public String generateRefreshToken(String userId, boolean isAdmin) {
    return buildToken(userId, isAdmin, "refresh", props.refreshSecret(), props.refreshTtlSeconds() * 1000);
  }

  private String buildToken(String userId, boolean isAdmin, String type, String secret, long ttlMs) {
    return buildToken(userId, isAdmin, type, secret, ttlMs, null);
  }

  private String buildToken(
      String userId, boolean isAdmin, String type, String secret, long ttlMs, String scope) {
    long now = System.currentTimeMillis();
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    var builder =
        Jwts.builder()
        .subject(userId)
        .claim("user_id", userId)
        .claim("is_admin", isAdmin)
        .claim("type", type)
        .issuedAt(new Date(now))
        .expiration(new Date(now + ttlMs));
    if (scope != null && !scope.isBlank()) {
      builder.claim("scope", scope);
    }
    return builder.signWith(key).compact();
  }

  public Claims parseAccessToken(String token) {
    return parse(token, props.accessSecret(), "access");
  }

  public Claims parseMcpAccessToken(String token) {
    return parse(token, props.accessSecret(), "mcp_access");
  }

  public Claims parseRefreshToken(String token) {
    return parse(token, props.refreshSecret(), "refresh");
  }

  private Claims parse(String token, String secret, String expectedType) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    Claims claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    if (!expectedType.equals(claims.get("type"))) {
      throw new IllegalArgumentException("invalid token type");
    }
    return claims;
  }
}
