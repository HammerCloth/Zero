package com.zero.service;

import com.zero.domain.Account;
import com.zero.domain.SnapshotItem;
import java.util.List;
import java.util.Map;

public final class BalanceLogic {

  private BalanceLogic() {}

  /** 存储前：负债账户正数余额转为负数 */
  public static double normalizeStoredBalance(String accountType, double input) {
    if ("credit".equals(accountType) && input > 0) {
      return -input;
    }
    return input;
  }

  /** 计算净资产：负债按负值计入 */
  public static double netWorth(List<SnapshotItem> items, Map<String, Account> accountsById) {
    double sum = 0;
    for (SnapshotItem it : items) {
      Account a = accountsById.get(it.getAccountId());
      if (a == null) {
        continue;
      }
      sum += effectiveBalance(a.getType(), it.getBalance());
    }
    return sum;
  }

  public static double effectiveBalance(String accountType, double stored) {
    return "credit".equals(accountType) ? -Math.abs(stored) : stored;
  }
}
