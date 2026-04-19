package com.zero.web;

import com.zero.config.AppEnvProperties;
import com.zero.domain.User;
import com.zero.security.JwtPrincipal;
import com.zero.service.AuthService;
import com.zero.support.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String REFRESH_COOKIE = "refresh_token";
  private static final String COOKIE_PATH = "/api/v1/auth";

  private final AuthService authService;
  private final LoginRateLimiter rateLimiter;
  private final AppEnvProperties env;

  public AuthController(AuthService authService, LoginRateLimiter rateLimiter, AppEnvProperties env) {
    this.authService = authService;
    this.rateLimiter = rateLimiter;
    this.env = env;
  }

  @GetMapping("/status")
  public Map<String, Boolean> status() {
    return Map.of("needs_setup", authService.needsSetup());
  }

  @PostMapping("/setup")
  public ResponseEntity<Map<String, Object>> setup(
      @RequestBody Map<String, String> body,
      HttpServletRequest request,
      HttpServletResponse response) {
    String username = body.get("username");
    String password = body.get("password");
    if (username == null || password == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "参数不合法"));
    }
    var result = authService.setup(username.trim(), password);
    writeRefreshCookie(response, request, result.refreshToken(), true);
    return ResponseEntity.ok(Map.of("user", result.user(), "access_token", result.accessToken()));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody Map<String, Object> body,
      HttpServletRequest request,
      HttpServletResponse response) {
    String ip = request.getRemoteAddr();
    rateLimiter.check(ip);
    String username = (String) body.get("username");
    String password = (String) body.get("password");
    boolean remember = Boolean.TRUE.equals(body.get("remember_me"));
    if (username == null || password == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "参数不合法"));
    }
    try {
      var result = authService.login(username.trim(), password, remember);
      rateLimiter.onSuccess(ip);
      writeRefreshCookie(response, request, result.refreshToken(), remember);
      return ResponseEntity.ok(Map.of("user", result.user(), "access_token", result.accessToken()));
    } catch (Exception e) {
      rateLimiter.onFailure(ip);
      throw e;
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refresh(
      @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return ResponseEntity.status(401).body(Map.of("error", "未登录或登录已过期"));
    }
    var result = authService.refresh(refreshToken);
    writeRefreshCookie(response, request, result.refreshToken(), true);
    return ResponseEntity.ok(Map.of("user", result.user(), "access_token", result.accessToken()));
  }

  @PostMapping("/logout")
  public Map<String, Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
    clearRefreshCookie(response, request);
    return Map.of("ok", true);
  }

  @GetMapping("/me")
  public User me() {
    JwtPrincipal p = CurrentUser.require();
    return authService.me(p.userId());
  }

  public record ChangePasswordBody(String current_password, String new_password) {}

  @PutMapping("/password")
  public Map<String, Boolean> password(@RequestBody ChangePasswordBody body) {
    JwtPrincipal p = CurrentUser.require();
    if (body.current_password() == null || body.new_password() == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "参数不合法");
    }
    authService.changePassword(p.userId(), body.current_password(), body.new_password());
    return Map.of("ok", true);
  }

  private void writeRefreshCookie(
      HttpServletResponse response, HttpServletRequest request, String token, boolean remember) {
    int maxAge = remember ? (int) (30L * 24 * 60 * 60) : (int) (24L * 60 * 60);
    boolean secure = "production".equalsIgnoreCase(env.env()) || request.isSecure();
    ResponseCookie cookie =
        ResponseCookie.from(REFRESH_COOKIE, token)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path(COOKIE_PATH)
            .maxAge(maxAge)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void clearRefreshCookie(HttpServletResponse response, HttpServletRequest request) {
    boolean secure = "production".equalsIgnoreCase(env.env()) || request.isSecure();
    ResponseCookie cookie =
        ResponseCookie.from(REFRESH_COOKIE, "")
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path(COOKIE_PATH)
            .maxAge(0)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
