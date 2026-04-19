package com.zero.service;

import com.zero.domain.UserOptionItem;
import com.zero.mapper.AccountMapper;
import com.zero.mapper.SnapshotMapper;
import com.zero.mapper.UserOptionMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserOptionService {

  /** 与默认种子（如 A、B、shared）一致：字母开头，仅含字母数字与下划线。 */
  private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,63}$");

  private final UserOptionMapper userOptionMapper;
  private final AccountMapper accountMapper;
  private final SnapshotMapper snapshotMapper;

  public UserOptionService(
      UserOptionMapper userOptionMapper, AccountMapper accountMapper, SnapshotMapper snapshotMapper) {
    this.userOptionMapper = userOptionMapper;
    this.accountMapper = accountMapper;
    this.snapshotMapper = snapshotMapper;
  }

  /** 无任何记录时写入默认种子（含历史用户首次访问）。 */
  @Transactional
  public void ensureSeeded(String userId) {
    if (userOptionMapper.countByUser(userId) > 0) {
      return;
    }
    insertSeed(userId);
  }

  @Transactional
  public void resetToDefaults(String userId) {
    userOptionMapper.deleteAllForUser(userId);
    insertSeed(userId);
  }

  private void insertSeed(String userId) {
    insertDimension(userId, UserOptionDefinitions.DIM_ACCOUNT_TYPE, UserOptionDefinitions.defaultAccountTypes());
    insertDimension(userId, UserOptionDefinitions.DIM_ACCOUNT_OWNER, UserOptionDefinitions.defaultAccountOwners());
    insertDimension(userId, UserOptionDefinitions.DIM_EVENT_CATEGORY, UserOptionDefinitions.defaultEventCategories());
  }

  private void insertDimension(String userId, String dimension, List<UserOptionDefinitions.SeedRow> rows) {
    for (UserOptionDefinitions.SeedRow r : rows) {
      UserOptionItem it = row(userId, dimension, r.key(), r.label(), r.sortOrder(), true);
      userOptionMapper.insert(it);
    }
  }

  private static UserOptionItem row(
      String userId, String dimension, String key, String label, int sortOrder, boolean enabled) {
    UserOptionItem it = new UserOptionItem();
    it.setId(newId());
    it.setUserId(userId);
    it.setDimension(dimension);
    it.setOptKey(key);
    it.setLabel(label);
    it.setSortOrder(sortOrder);
    it.setEnabled(enabled);
    return it;
  }

  public Map<String, List<Map<String, Object>>> getOptions(String userId) {
    ensureSeeded(userId);
    List<UserOptionItem> all = userOptionMapper.listByUser(userId);
    Map<String, List<Map<String, Object>>> out = new LinkedHashMap<>();
    out.put(UserOptionDefinitions.DIM_ACCOUNT_TYPE, new ArrayList<>());
    out.put(UserOptionDefinitions.DIM_ACCOUNT_OWNER, new ArrayList<>());
    out.put(UserOptionDefinitions.DIM_EVENT_CATEGORY, new ArrayList<>());
    for (UserOptionItem it : all) {
      out.computeIfAbsent(it.getDimension(), k -> new ArrayList<>()).add(toDto(it));
    }
    return out;
  }

  private static Map<String, Object> toDto(UserOptionItem it) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("key", it.getOptKey());
    m.put("label", it.getLabel());
    m.put("sortOrder", it.getSortOrder());
    m.put("enabled", it.isEnabled());
    return m;
  }

  public record OptionIn(String key, String label, Integer sortOrder, Boolean enabled) {}

  @Transactional
  public void replaceDimension(String userId, String dimension, List<OptionIn> items) {
    ensureSeeded(userId);
    validateDimension(dimension);
    if (items == null || items.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少保留一条选项");
    }
    List<UserOptionItem> existing = userOptionMapper.listByUser(userId).stream()
        .filter(x -> dimension.equals(x.getDimension()))
        .collect(Collectors.toList());
    Map<String, UserOptionItem> oldByKey =
        existing.stream().collect(Collectors.toMap(UserOptionItem::getOptKey, x -> x, (a, b) -> a));

    List<String> newKeys = new ArrayList<>();
    for (OptionIn in : items) {
      validateKey(in.key());
      if (in.label() == null || in.label().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
      }
      newKeys.add(in.key());
    }
    if (newKeys.stream().distinct().count() != newKeys.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    long enabledCount = items.stream().filter(x -> x.enabled() == null || x.enabled()).count();
    if (enabledCount == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少启用一条选项");
    }

    for (String oldKey : oldByKey.keySet()) {
      if (!newKeys.contains(oldKey)) {
        assertKeyNotReferenced(userId, dimension, oldKey);
      }
    }

    userOptionMapper.deleteByUserAndDimension(userId, dimension);
    for (OptionIn in : items) {
      int so = in.sortOrder() != null ? in.sortOrder() : 0;
      boolean en = in.enabled() == null || in.enabled();
      userOptionMapper.insert(row(userId, dimension, in.key(), in.label().trim(), so, en));
    }
  }

  private void assertKeyNotReferenced(String userId, String dimension, String key) {
    if (UserOptionDefinitions.DIM_ACCOUNT_TYPE.equals(dimension)) {
      if (accountMapper.countByUserAndType(userId, key) > 0) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "仍有账户使用该类型");
      }
    } else if (UserOptionDefinitions.DIM_ACCOUNT_OWNER.equals(dimension)) {
      if (accountMapper.countByUserAndOwner(userId, key) > 0) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "仍有账户使用该归属");
      }
    } else if (UserOptionDefinitions.DIM_EVENT_CATEGORY.equals(dimension)) {
      if (snapshotMapper.countEventsByUserAndCategory(userId, key) > 0) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "仍有大事记使用该分类");
      }
    }
  }

  private static void validateDimension(String dimension) {
    if (!UserOptionDefinitions.DIM_ACCOUNT_TYPE.equals(dimension)
        && !UserOptionDefinitions.DIM_ACCOUNT_OWNER.equals(dimension)
        && !UserOptionDefinitions.DIM_EVENT_CATEGORY.equals(dimension)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
  }

  private static void validateKey(String key) {
    if (key == null || !KEY_PATTERN.matcher(key).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
  }

  public void requireValidAccountType(String userId, String type) {
    requireValidKey(userId, UserOptionDefinitions.DIM_ACCOUNT_TYPE, type);
  }

  public void requireValidAccountOwner(String userId, String owner) {
    requireValidKey(userId, UserOptionDefinitions.DIM_ACCOUNT_OWNER, owner);
  }

  public void requireValidEventCategory(String userId, String category) {
    requireValidKey(userId, UserOptionDefinitions.DIM_EVENT_CATEGORY, category);
  }

  private void requireValidKey(String userId, String dimension, String key) {
    ensureSeeded(userId);
    if (key == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
    }
    List<UserOptionItem> all = userOptionMapper.listByUser(userId);
    for (UserOptionItem it : all) {
      if (dimension.equals(it.getDimension()) && key.equals(it.getOptKey()) && it.isEnabled()) {
        return;
      }
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
  }

  private static String newId() {
    return java.util.UUID.randomUUID().toString().replace("-", "");
  }
}
