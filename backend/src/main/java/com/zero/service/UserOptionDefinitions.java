package com.zero.service;

import java.util.List;

/** 与历史硬编码账户/大事记枚举等价的默认选项。 */
public final class UserOptionDefinitions {

  public static final String DIM_ACCOUNT_TYPE = "account_type";
  public static final String DIM_ACCOUNT_OWNER = "account_owner";
  public static final String DIM_EVENT_CATEGORY = "event_category";

  public record SeedRow(String key, String label, int sortOrder) {}

  private UserOptionDefinitions() {}

  public static List<SeedRow> defaultAccountTypes() {
    return List.of(
        new SeedRow("cash", "现金", 0),
        new SeedRow("deposit", "固收", 1),
        new SeedRow("fund", "基金", 2),
        new SeedRow("pension", "养老", 3),
        new SeedRow("housing_fund", "公积金", 4),
        new SeedRow("credit", "负债", 5));
  }

  public static List<SeedRow> defaultAccountOwners() {
    return List.of(
        new SeedRow("A", "成员 A", 0),
        new SeedRow("B", "成员 B", 1),
        new SeedRow("shared", "共同", 2));
  }

  public static List<SeedRow> defaultEventCategories() {
    return List.of(
        new SeedRow("rent", "房租", 0),
        new SeedRow("travel", "旅行", 1),
        new SeedRow("medical", "医疗", 2),
        new SeedRow("appliance", "家电装修", 3),
        new SeedRow("social", "人情往来", 4),
        new SeedRow("other", "其他", 5));
  }
}
