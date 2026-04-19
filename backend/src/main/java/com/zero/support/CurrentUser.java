package com.zero.support;

import com.zero.security.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class CurrentUser {

  private CurrentUser() {}

  public static JwtPrincipal require() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof JwtPrincipal p)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
    }
    return p;
  }
}
