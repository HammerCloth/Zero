package com.zero.web;

import com.zero.domain.User;
import com.zero.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public Map<String, List<User>> list() {
    return Map.of("users", userService.listAll());
  }

  public record CreateBody(String username, String password, boolean isAdmin) {}

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, User> create(@RequestBody CreateBody body) {
    User u = userService.create(body.username(), body.password(), body.isAdmin());
    return Map.of("user", u);
  }

  public record ResetPasswordBody(String password) {}

  @PutMapping("/{id}/password")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public Map<String, Boolean> resetPassword(@PathVariable String id, @RequestBody ResetPasswordBody body) {
    userService.resetPassword(id, body.password());
    return Map.of("ok", true);
  }
}
