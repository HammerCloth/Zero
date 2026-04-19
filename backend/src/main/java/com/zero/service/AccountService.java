package com.zero.service;

import com.zero.domain.Account;
import com.zero.mapper.AccountMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

  private final AccountMapper accountMapper;
  private final UserOptionService userOptionService;

  public AccountService(AccountMapper accountMapper, UserOptionService userOptionService) {
    this.accountMapper = accountMapper;
    this.userOptionService = userOptionService;
  }

  public List<Account> list(String userId) {
    return accountMapper.listActiveByUser(userId);
  }

  @Transactional
  public Account create(String userId, String name, String type, String owner) {
    userOptionService.requireValidAccountType(userId, type);
    userOptionService.requireValidAccountOwner(userId, owner);
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    List<Account> existing = accountMapper.listAllByUser(userId);
    int maxOrder =
        existing.stream().mapToInt(Account::getSortOrder).max().orElse(0);
    Account a = new Account();
    a.setId(newId());
    a.setUserId(userId);
    a.setName(name.trim());
    a.setType(type);
    a.setOwner(owner);
    a.setSortOrder(maxOrder + 1);
    a.setActive(true);
    accountMapper.insert(a);
    return a;
  }

  @Transactional
  public void update(String userId, String id, String name, String type, String owner) {
    Account existing = accountMapper.findByIdAndUser(id, userId);
    if (existing == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账户不存在");
    }
    userOptionService.requireValidAccountType(userId, type);
    userOptionService.requireValidAccountOwner(userId, owner);
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    existing.setName(name.trim());
    existing.setType(type);
    existing.setOwner(owner);
    accountMapper.update(existing);
  }

  @Transactional
  public void deactivate(String userId, String id) {
    Account existing = accountMapper.findByIdAndUser(id, userId);
    if (existing == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账户不存在");
    }
    accountMapper.deactivate(id);
  }

  @Transactional
  public void reorder(String userId, List<String> accountIds) {
    if (accountIds == null || accountIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    for (int i = 0; i < accountIds.size(); i++) {
      String aid = accountIds.get(i);
      Account a = accountMapper.findByIdAndUser(aid, userId);
      if (a == null || !a.isActive()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
      }
      accountMapper.updateSort(aid, userId, i);
    }
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
