package com.zero.service;

import com.zero.domain.Account;
import com.zero.domain.Snapshot;
import com.zero.domain.SnapshotEvent;
import com.zero.domain.SnapshotItem;
import com.zero.mapper.AccountMapper;
import com.zero.mapper.SnapshotMapper;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SnapshotService {

  public record ItemIn(String accountId, Double balance) {}

  public record EventIn(String category, String description, Double amount) {}

  private final SnapshotMapper snapshotMapper;
  private final AccountMapper accountMapper;
  private final UserOptionService userOptionService;

  public SnapshotService(
      SnapshotMapper snapshotMapper, AccountMapper accountMapper, UserOptionService userOptionService) {
    this.snapshotMapper = snapshotMapper;
    this.accountMapper = accountMapper;
    this.userOptionService = userOptionService;
  }

  public List<Map<String, Object>> listSummaries(String userId) {
    Map<String, Account> accounts = loadAccountsById(userId);
    List<Map<String, Object>> out = new ArrayList<>();
    for (Snapshot s : snapshotMapper.listSnapshotsByUser(userId)) {
      List<SnapshotItem> items = snapshotMapper.listItems(s.getId());
      double nw = BalanceLogic.netWorth(items, accounts);
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("id", s.getId());
      row.put("date", s.getDate());
      row.put("createdAt", s.getCreatedAt());
      row.put("netWorth", nw);
      out.add(row);
    }
    return out;
  }

  public Map<String, Object> getDetail(String userId, String snapshotId) {
    Snapshot s = snapshotMapper.findSnapshotById(snapshotId);
    if (s == null || !userId.equals(s.getUserId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "快照不存在");
    }
    return buildDetailMap(s, accountsMap(userId));
  }

  public Map<String, Object> getLatestOrNull(String userId) {
    Snapshot s = snapshotMapper.findLatestSnapshot(userId);
    Map<String, Object> out = new HashMap<>();
    if (s == null) {
      out.put("snapshot", null);
    } else {
      out.put("snapshot", buildDetailMap(s, accountsMap(userId)));
    }
    return out;
  }

  public Map<String, Object> findForDate(String userId, String date) {
    parseDate(date);
    String sid = snapshotMapper.findSnapshotIdByUserAndDate(userId, date);
    Map<String, Object> out = new LinkedHashMap<>();
    if (sid == null) {
      out.put("snapshot", null);
    } else {
      out.put("snapshot", Map.of("id", sid, "date", date));
    }
    return out;
  }

  public Map<String, Object> listDatesInRange(String userId, String from, String to) {
    parseDate(from);
    parseDate(to);
    return Map.of("dates", snapshotMapper.listSnapshotDatesBetween(userId, from, to));
  }

  @Transactional
  public Map<String, Object> create(
      String userId, String createdBy, String date, String note, List<ItemIn> itemsIn, List<EventIn> eventsIn) {
    parseDate(date);
    if (snapshotMapper.countByUserAndDate(userId, date) > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "该日期已有快照");
    }
    List<Account> active = accountMapper.listActiveByUser(userId);
    validateItemsComplete(active, itemsIn);
    Map<String, Account> byId = active.stream().collect(Collectors.toMap(Account::getId, Function.identity()));

    String sid = newId();
    Snapshot snap = new Snapshot();
    snap.setId(sid);
    snap.setUserId(userId);
    snap.setDate(date);
    snap.setNote(note);
    snap.setCreatedBy(createdBy);
    snapshotMapper.insertSnapshot(snap);

    persistItems(sid, itemsIn, byId);
    persistEvents(sid, eventsIn == null ? List.of() : eventsIn, userId);

    Snapshot loaded = snapshotMapper.findSnapshotById(sid);
    return Map.of("snapshot", buildDetailMap(Objects.requireNonNull(loaded), accountsMap(userId)));
  }

  @Transactional
  public Map<String, Object> update(
      String userId, String snapshotId, String date, String note, List<ItemIn> itemsIn, List<EventIn> eventsIn) {
    Snapshot existing = snapshotMapper.findSnapshotById(snapshotId);
    if (existing == null || !userId.equals(existing.getUserId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "快照不存在");
    }
    parseDate(date);
    if (snapshotMapper.countByUserAndDateExcluding(userId, date, snapshotId) > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "该日期已有快照");
    }
    List<Account> active = accountMapper.listActiveByUser(userId);
    validateItemsComplete(active, itemsIn);
    Map<String, Account> byId = active.stream().collect(Collectors.toMap(Account::getId, Function.identity()));

    existing.setDate(date);
    existing.setNote(note);
    snapshotMapper.updateSnapshot(existing);

    snapshotMapper.deleteItemsForSnapshot(snapshotId);
    snapshotMapper.deleteEventsForSnapshot(snapshotId);
    persistItems(snapshotId, itemsIn, byId);
    persistEvents(snapshotId, eventsIn == null ? List.of() : eventsIn, userId);

    Snapshot loaded = snapshotMapper.findSnapshotById(snapshotId);
    return Map.of("snapshot", buildDetailMap(Objects.requireNonNull(loaded), accountsMap(userId)));
  }

  @Transactional
  public void delete(String userId, String snapshotId) {
    int n = snapshotMapper.deleteSnapshot(snapshotId, userId);
    if (n == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "快照不存在");
    }
  }

  private void persistItems(String snapshotId, List<ItemIn> itemsIn, Map<String, Account> activeById) {
    for (ItemIn in : itemsIn) {
      Account acc = activeById.get(in.accountId());
      if (acc == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
      }
      double raw = in.balance();
      double stored = BalanceLogic.normalizeStoredBalance(acc.getType(), raw);
      SnapshotItem row = new SnapshotItem();
      row.setId(newId());
      row.setSnapshotId(snapshotId);
      row.setAccountId(in.accountId());
      row.setBalance(stored);
      snapshotMapper.insertItem(row);
    }
  }

  private void persistEvents(String snapshotId, List<EventIn> eventsIn, String userId) {
    for (EventIn ev : eventsIn) {
      if (ev.description() == null || ev.description().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
      }
      if (ev.amount() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
      }
      userOptionService.requireValidEventCategory(userId, ev.category());
      SnapshotEvent e = new SnapshotEvent();
      e.setId(newId());
      e.setSnapshotId(snapshotId);
      e.setCategory(ev.category());
      e.setDescription(ev.description().trim());
      e.setAmount(ev.amount());
      snapshotMapper.insertEvent(e);
    }
  }

  private static void validateItemsComplete(List<Account> active, List<ItemIn> itemsIn) {
    if (itemsIn == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "存在未填写余额的账户");
    }
    long distinctAccounts = itemsIn.stream().map(ItemIn::accountId).distinct().count();
    if (distinctAccounts != itemsIn.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    Map<String, ItemIn> byAccount =
        itemsIn.stream().collect(Collectors.toMap(ItemIn::accountId, Function.identity(), (a, b) -> a));
    for (Account a : active) {
      ItemIn in = byAccount.get(a.getId());
      if (in == null || in.balance() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "存在未填写余额的账户");
      }
    }
    for (String aid : byAccount.keySet()) {
      if (active.stream().noneMatch(x -> x.getId().equals(aid))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
      }
    }
  }

  private Map<String, Object> buildDetailMap(Snapshot s, Map<String, Account> accounts) {
    List<SnapshotItem> items = snapshotMapper.listItems(s.getId());
    List<Map<String, Object>> itemViews = new ArrayList<>();
    for (SnapshotItem it : items) {
      Account acc = accounts.get(it.getAccountId());
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("accountId", it.getAccountId());
      m.put("balance", it.getBalance());
      if (acc != null) {
        m.put("accountName", acc.getName());
        m.put("type", acc.getType());
        m.put("owner", acc.getOwner());
      }
      itemViews.add(m);
    }
    List<SnapshotEvent> evs = snapshotMapper.listEvents(s.getId());
    List<Map<String, Object>> eventViews = new ArrayList<>();
    for (SnapshotEvent e : evs) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", e.getId());
      m.put("category", e.getCategory());
      m.put("description", e.getDescription());
      m.put("amount", e.getAmount());
      m.put("createdAt", e.getCreatedAt());
      eventViews.add(m);
    }
    Map<String, Object> snap = new LinkedHashMap<>();
    snap.put("id", s.getId());
    snap.put("date", s.getDate());
    snap.put("note", s.getNote());
    snap.put("createdAt", s.getCreatedAt());
    snap.put("createdBy", s.getCreatedBy());
    snap.put("items", itemViews);
    snap.put("events", eventViews);
    return snap;
  }

  private Map<String, Account> accountsMap(String userId) {
    return accountMapper.listAllByUser(userId).stream()
        .collect(Collectors.toMap(Account::getId, Function.identity()));
  }

  private Map<String, Account> loadAccountsById(String userId) {
    return accountsMap(userId);
  }

  private static void parseDate(String date) {
    try {
      LocalDate.parse(date);
    } catch (DateTimeParseException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
