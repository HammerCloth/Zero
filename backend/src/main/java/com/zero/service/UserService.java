package com.zero.service;

import com.zero.domain.User;
import com.zero.mapper.UserMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserOptionService userOptionService;

  public UserService(
      UserMapper userMapper, PasswordEncoder passwordEncoder, UserOptionService userOptionService) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.userOptionService = userOptionService;
  }

  public List<User> listAll() {
    return userMapper.listAll().stream()
        .peek(u -> u.setPasswordHash(null))
        .collect(Collectors.toList());
  }

  @Transactional
  public User create(String username, String password, boolean isAdmin) {
    validatePassword(password);
    if (username == null || username.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    String trimmed = username.trim();
    if (userMapper.findByUsername(trimmed) != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
    }
    User u = new User();
    u.setId(newId());
    u.setUsername(trimmed);
    u.setPasswordHash(passwordEncoder.encode(password));
    u.setAdmin(isAdmin);
    u.setMustChangePassword(true);
    userMapper.insert(u);
    userOptionService.ensureSeeded(u.getId());
    u.setPasswordHash(null);
    return u;
  }

  @Transactional
  public void resetPassword(String targetUserId, String newPassword) {
    validatePassword(newPassword);
    User u = userMapper.findById(targetUserId);
    if (u == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "资源不存在");
    }
    userMapper.updatePassword(targetUserId, passwordEncoder.encode(newPassword), true);
  }

  private static void validatePassword(String password) {
    if (password == null || password.length() < 8) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码至少 8 位");
    }
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
