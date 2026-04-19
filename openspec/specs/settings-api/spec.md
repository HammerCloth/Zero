# settings-api Specification

## Purpose

定义每用户可配置的枚举选项（账户类型、账户归属、大事记分类）的读写 API。数据存于 `user_option_items`（Flyway V2），按 **`user_id`** 隔离；首次访问时由后端种子化默认值。

## Requirements

### Requirement: 读取全部选项

系统 SHALL 提供 `GET /api/v1/settings/options` 返回当前用户三个维度下的选项列表。

#### Scenario: 成功

- **WHEN** 用户已登录
- **THEN** 返回 200，JSON 顶层键包含 `account_type`、`account_owner`、`event_category`（或等价维度名），每项为 `{ key, label, sortOrder, enabled }` 的数组

### Requirement: 替换某维度选项

系统 SHALL 提供 `PUT /api/v1/settings/options/{dimension}`，请求体为 `{ "items": [ { key, label, sortOrder, enabled }, ... ] }`，整维替换。

#### Scenario: 路径别名

- **WHEN** `dimension` 为 `account-type`、`account-owner`、`event-category`（kebab-case）
- **THEN** SHALL 映射到内部维度 `account_type`、`account_owner`、`event_category`

#### Scenario: 校验失败

- **WHEN** 空 label、重复 key、非法 key 格式、或启用项为 0
- **THEN** 返回 400，消息如 `参数不合法` 或 `至少保留一条选项` / `至少启用一条选项`

#### Scenario: 删除仍被引用的 key

- **WHEN** 请求中去掉仍被账户/大事记使用的 key
- **THEN** 返回 **409**，消息说明仍有实体使用该类型/归属/分类（见 `UserOptionService.assertKeyNotReferenced`）

### Requirement: 恢复默认

系统 SHALL 提供 `POST /api/v1/settings/options/reset`，删除当前用户全部选项记录并重新插入默认种子。

#### Scenario: 成功

- **WHEN** 用户已登录
- **THEN** 返回 200 和 `{"ok": true}`

### Requirement: 选项 key 格式

选项 `key` SHALL 满足后端校验：字母开头，仅含字母、数字、下划线；长度在实现允许范围内（与默认种子 `A`/`B`/`shared` 等兼容）。

### Requirement: 账户与大事记校验

创建/更新账户时 `type`、`owner`，以及大事记 `category`，SHALL 校验为当前用户对应维度下**已启用**的选项 key（见 `AccountService` / `SnapshotService` 等）。
