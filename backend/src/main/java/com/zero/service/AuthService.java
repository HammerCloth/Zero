package com.zero.service;

import com.zero.domain.User;
import com.zero.mapper.AccountMapper;
import com.zero.mapper.UserMapper;
import com.zero.security.JwtService;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserMapper userMapper;
  private final AccountMapper accountMapper;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final UserOptionService userOptionService;

  public AuthService(
      UserMapper userMapper,
      AccountMapper accountMapper,
      JwtService jwtService,
      PasswordEncoder passwordEncoder,
      UserOptionService userOptionService) {
    this.userMapper = userMapper;
    this.accountMapper = accountMapper;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.userOptionService = userOptionService;
  }

  public boolean needsSetup() {
    return userMapper.countUsers() == 0;
  }

  @Transactional
  public LoginResult setup(String username, String password) {
    if (userMapper.countUsers() > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "初始化已完成");
    }
    validatePassword(password);
    User u = new User();
    u.setId(newId());
    u.setUsername(username);
    u.setPasswordHash(passwordEncoder.encode(password));
    u.setAdmin(true);
    u.setMustChangePassword(false);
    userMapper.insert(u);
    DefaultAccountsFactory.insertDefaults(accountMapper, u.getId());
    userOptionService.ensureSeeded(u.getId());
    return tokensForUser(u);
  }

  public LoginResult login(String username, String password, boolean remember) {
    User u = userMapper.findByUsername(username);
    if (u == null || !passwordEncoder.matches(password, u.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
    }
    return tokensForUser(u);
  }

  public LoginResult refresh(String refreshToken) {
    try {
      Claims c = jwtService.parseRefreshToken(refreshToken);
      String uid = c.getSubject();
      User u = userMapper.findById(uid);
      if (u == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
      }
      return tokensForUser(u);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
    }
  }

  public User me(String userId) {
    User u = userMapper.findById(userId);
    if (u == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "资源不存在");
    }
    u.setPasswordHash(null);
    return u;
  }

  public void changePassword(String userId, String currentPassword, String newPassword) {
    validatePassword(newPassword);
    User u = userMapper.findById(userId);
    if (u == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "资源不存在");
    }
    if (!passwordEncoder.matches(currentPassword, u.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码错误");
    }
    userMapper.updatePassword(userId, passwordEncoder.encode(newPassword), false);
  }

  private LoginResult tokensForUser(User u) {
    String access = jwtService.generateAccessToken(u.getId(), u.isAdmin());
    String refresh = jwtService.generateRefreshToken(u.getId(), u.isAdmin());
    u.setPasswordHash(null);
    return new LoginResult(u, access, refresh);
  }

  private static void validatePassword(String password) {
    if (password == null || password.length() < 8) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码至少 8 位");
    }
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public record LoginResult(User user, String accessToken, String refreshToken) {}
}
