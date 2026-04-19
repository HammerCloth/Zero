package com.zero.web;

import com.zero.domain.Account;
import com.zero.service.AccountService;
import com.zero.support.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  public Map<String, List<Account>> list() {
    String uid = CurrentUser.require().userId();
    return Map.of("accounts", accountService.list(uid));
  }

  public record CreateBody(String name, String type, String owner) {}

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Account> create(@RequestBody CreateBody body) {
    String uid = CurrentUser.require().userId();
    Account a = accountService.create(uid, body.name(), body.type(), body.owner());
    return Map.of("account", a);
  }

  public record UpdateBody(String name, String type, String owner) {}

  @PutMapping("/{id}")
  public Map<String, Boolean> update(@PathVariable String id, @RequestBody UpdateBody body) {
    String uid = CurrentUser.require().userId();
    accountService.update(uid, id, body.name(), body.type(), body.owner());
    return Map.of("ok", true);
  }

  @DeleteMapping("/{id}")
  public Map<String, Boolean> delete(@PathVariable String id) {
    String uid = CurrentUser.require().userId();
    accountService.deactivate(uid, id);
    return Map.of("ok", true);
  }

  public record ReorderBody(List<String> accountIds) {}

  @PutMapping("/reorder")
  public Map<String, Boolean> reorder(@RequestBody ReorderBody body) {
    String uid = CurrentUser.require().userId();
    accountService.reorder(uid, body.accountIds());
    return Map.of("ok", true);
  }
}
