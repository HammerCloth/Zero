package com.zero.service;

import com.zero.domain.Account;
import com.zero.domain.Snapshot;
import com.zero.domain.SnapshotItem;
import com.zero.mapper.AccountMapper;
import com.zero.mapper.SnapshotMapper;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private final SnapshotMapper snapshotMapper;
  private final AccountMapper accountMapper;

  public DashboardService(SnapshotMapper snapshotMapper, AccountMapper accountMapper) {
    this.snapshotMapper = snapshotMapper;
    this.accountMapper = accountMapper;
  }

  public Map<String, Object> summary(String userId) {
    List<Snapshot> desc = snapshotMapper.listSnapshotsByUser(userId);
    if (desc.isEmpty()) {
      return Map.of(
          "netWorth", 0.0,
          "monthlyChange", 0.0,
          "annualChange", 0.0,
          "annualizedReturn", 0.0);
    }
    Map<String, Account> accounts = accountsById(userId);
    Snapshot latest = desc.get(0);
    double nw = nwForSnapshot(latest.getId(), accounts);

    LocalDate latestDate = LocalDate.parse(latest.getDate());
    LocalDate monthAgo = latestDate.minusMonths(1);
    double nwMonthAgo = nwLatestOnOrBefore(userId, desc, monthAgo, accounts);

    LocalDate yearAgo = latestDate.minusYears(1);
    double nwYearAgo = nwLatestOnOrBefore(userId, desc, yearAgo, accounts);

    return Map.of(
        "netWorth", nw,
        "monthlyChange", nw - nwMonthAgo,
        "annualChange", nw - nwYearAgo,
        "annualizedReturn", annualizedReturn(desc, accounts));
  }

  public Map<String, Object> trend(String userId, String range) {
    List<Snapshot> desc = snapshotMapper.listSnapshotsByUser(userId);
    Map<String, Account> accounts = accountsById(userId);
    LocalDate from = rangeFromOrNull(range == null ? "all" : range);
    List<Map<String, Object>> points = new ArrayList<>();
    for (int i = desc.size() - 1; i >= 0; i--) {
      Snapshot s = desc.get(i);
      LocalDate d = LocalDate.parse(s.getDate());
      if (from != null && d.isBefore(from)) {
        continue;
      }
      double nw = nwForSnapshot(s.getId(), accounts);
      Map<String, Object> pt = new LinkedHashMap<>();
      pt.put("date", s.getDate());
      pt.put("netWorth", nw);
      points.add(pt);
    }
    return Map.of("points", points);
  }

  public Map<String, Object> stackedByType(String userId, String range) {
    List<Snapshot> desc = snapshotMapper.listSnapshotsByUser(userId);
    if (desc.isEmpty()) {
      return Map.of("points", List.of());
    }
    Map<String, Account> accounts = accountsById(userId);
    LocalDate from = rangeFromOrNull(range == null ? "all" : range);
    List<Map<String, Object>> points = new ArrayList<>();
    for (int i = desc.size() - 1; i >= 0; i--) {
      Snapshot s = desc.get(i);
      LocalDate d = LocalDate.parse(s.getDate());
      if (from != null && d.isBefore(from)) {
        continue;
      }
      Map<String, Double> byType = compositionByTypeForSnapshot(s.getId(), accounts);
      Map<String, Object> pt = new LinkedHashMap<>();
      pt.put("date", s.getDate());
      pt.put("byType", byType);
      points.add(pt);
    }
    return Map.of("points", points);
  }

  public Map<String, Object> accountTrends(String userId, String range) {
    List<Snapshot> desc = snapshotMapper.listSnapshotsByUser(userId);
    List<Account> active = accountMapper.listActiveByUser(userId);
    if (desc.isEmpty() || active.isEmpty()) {
      return Map.of("accounts", List.of());
    }
    Map<String, Account> accounts = accountsById(userId);
    LocalDate from = rangeFromOrNull(range == null ? "all" : range);
    List<Snapshot> inRange = new ArrayList<>();
    for (int i = desc.size() - 1; i >= 0; i--) {
      Snapshot s = desc.get(i);
      LocalDate d = LocalDate.parse(s.getDate());
      if (from != null && d.isBefore(from)) {
        continue;
      }
      inRange.add(s);
    }
    List<Map<String, Object>> accountRows = new ArrayList<>();
    for (Account acc : active) {
      Double lastRaw = null;
      List<Map<String, Object>> pts = new ArrayList<>();
      for (Snapshot s : inRange) {
        Double raw = findBalanceForAccount(s.getId(), acc.getId());
        if (raw != null) {
          lastRaw = raw;
        }
        if (lastRaw == null) {
          continue;
        }
        double eff = BalanceLogic.effectiveBalance(acc.getType(), lastRaw);
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("date", s.getDate());
        p.put("balance", eff);
        pts.add(p);
      }
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("accountId", acc.getId());
      row.put("name", acc.getName());
      row.put("type", acc.getType());
      row.put("points", pts);
      accountRows.add(row);
    }
    return Map.of("accounts", accountRows);
  }

  public Map<String, Object> composition(String userId) {
    List<Snapshot> desc = snapshotMapper.listSnapshotsByUser(userId);
    if (desc.isEmpty()) {
      return Map.of("byType", Map.of(), "byOwner", Map.of());
    }
    Map<String, Account> accounts = accountsById(userId);
    Snapshot latest = desc.get(0);
    List<SnapshotItem> items = snapshotMapper.listItems(latest.getId());
    Map<String, Double> byType = new HashMap<>();
    Map<String, Double> byOwner = new HashMap<>();
    for (SnapshotItem it : items) {
      Account a = accounts.get(it.getAccountId());
      if (a == null) {
        continue;
      }
      double eff = BalanceLogic.effectiveBalance(a.getType(), it.getBalance());
      byType.merge(a.getType(), eff, Double::sum);
      byOwner.merge(a.getOwner(), eff, Double::sum);
    }
    return Map.of("byType", byType, "byOwner", byOwner);
  }

  public Map<String, Object> monthlyGrowth(String userId, Integer year) {
    int y = year != null ? year : LocalDate.now().getYear();
    List<Snapshot> desc = snapshotMapper.listSnapshotsByUser(userId);
    Map<String, Account> accounts = accountsById(userId);
    List<Map<String, Object>> points = new ArrayList<>();
    double cumulative = 0;
    for (int m = 1; m <= 12; m++) {
      YearMonth ym = YearMonth.of(y, m);
      LocalDate end = ym.atEndOfMonth();
      LocalDate prevEnd = ym.minusMonths(1).atEndOfMonth();
      double nwEnd = nwLatestOnOrBefore(userId, desc, end, accounts);
      double nwPrev = nwLatestOnOrBefore(userId, desc, prevEnd, accounts);
      double change = nwEnd - nwPrev;
      cumulative += change;
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("month", String.format("%02d", m));
      row.put("change", change);
      row.put("cumulativeChange", cumulative);
      points.add(row);
    }
    return Map.of("year", y, "points", points);
  }

  private static LocalDate rangeFromOrNull(String range) {
    return switch (range) {
      case "3m" -> LocalDate.now().minusMonths(3);
      case "6m" -> LocalDate.now().minusMonths(6);
      case "1y" -> LocalDate.now().minusYears(1);
      default -> null;
    };
  }

  private Map<String, Double> compositionByTypeForSnapshot(String snapshotId, Map<String, Account> accounts) {
    List<SnapshotItem> items = snapshotMapper.listItems(snapshotId);
    Map<String, Double> byType = new HashMap<>();
    for (SnapshotItem it : items) {
      Account a = accounts.get(it.getAccountId());
      if (a == null) {
        continue;
      }
      double eff = BalanceLogic.effectiveBalance(a.getType(), it.getBalance());
      byType.merge(a.getType(), eff, Double::sum);
    }
    return byType;
  }

  private Double findBalanceForAccount(String snapshotId, String accountId) {
    for (SnapshotItem it : snapshotMapper.listItems(snapshotId)) {
      if (accountId.equals(it.getAccountId())) {
        return it.getBalance();
      }
    }
    return null;
  }

  private double annualizedReturn(List<Snapshot> desc, Map<String, Account> accounts) {
    if (desc.size() < 2) {
      return 0.0;
    }
    Snapshot latest = desc.get(0);
    Snapshot earliest = desc.get(desc.size() - 1);
    LocalDate dLatest = LocalDate.parse(latest.getDate());
    LocalDate dEarliest = LocalDate.parse(earliest.getDate());
    long days = ChronoUnit.DAYS.between(dEarliest, dLatest);
    if (days <= 0) {
      return 0.0;
    }
    double nwL = nwForSnapshot(latest.getId(), accounts);
    double nwE = nwForSnapshot(earliest.getId(), accounts);
    if (nwE <= 0) {
      return 0.0;
    }
    return Math.pow(nwL / nwE, 365.0 / days) - 1.0;
  }

  private double nwForSnapshot(String snapshotId, Map<String, Account> accounts) {
    List<SnapshotItem> items = snapshotMapper.listItems(snapshotId);
    return BalanceLogic.netWorth(items, accounts);
  }

  private double nwLatestOnOrBefore(
      String userId, List<Snapshot> desc, LocalDate target, Map<String, Account> accounts) {
    for (Snapshot s : desc) {
      LocalDate d = LocalDate.parse(s.getDate());
      if (!d.isAfter(target)) {
        return nwForSnapshot(s.getId(), accounts);
      }
    }
    return 0.0;
  }

  private Map<String, Account> accountsById(String userId) {
    return accountMapper.listAllByUser(userId).stream()
        .collect(Collectors.toMap(Account::getId, Function.identity()));
  }
}
