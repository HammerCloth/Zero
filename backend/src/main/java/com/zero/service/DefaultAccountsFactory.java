package com.zero.service;

import com.zero.domain.Account;
import com.zero.mapper.AccountMapper;
import java.util.List;
import java.util.UUID;

public final class DefaultAccountsFactory {

  private DefaultAccountsFactory() {}

  public static void insertDefaults(AccountMapper mapper, String userId) {
    List<AccountTemplate> defs =
        List.of(
            new AccountTemplate("A工资卡", "cash", "A", 1),
            new AccountTemplate("B工资卡", "cash", "B", 2),
            new AccountTemplate("小荷包", "cash", "shared", 3),
            new AccountTemplate("招行存单", "deposit", "shared", 4),
            new AccountTemplate("月月宝", "deposit", "shared", 5),
            new AccountTemplate("国内标普QDII", "fund", "A", 6),
            new AccountTemplate("海外基金", "fund", "B", 7),
            new AccountTemplate("个人养老金", "pension", "A", 8),
            new AccountTemplate("A住房公积金", "housing_fund", "A", 9),
            new AccountTemplate("B住房公积金", "housing_fund", "B", 10),
            new AccountTemplate("信用卡", "credit", "A", 11));
    for (AccountTemplate t : defs) {
      Account a = new Account();
      a.setId(UUID.randomUUID().toString().replace("-", ""));
      a.setUserId(userId);
      a.setName(t.name());
      a.setType(t.type());
      a.setOwner(t.owner());
      a.setSortOrder(t.sort());
      a.setActive(true);
      mapper.insert(a);
    }
  }

  private record AccountTemplate(String name, String type, String owner, int sort) {}
}
