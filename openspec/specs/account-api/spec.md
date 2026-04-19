# account-api Specification

## Purpose

定义当前用户账户的 CRUD 与排序 API（`/api/v1/accounts`）。`type`、`owner` 的合法取值来自 **`GET /api/v1/settings/options`** 中对应维度下**已启用**的 key，而非固定枚举（默认种子见 `UserOptionDefinitions`）。JSON 字段对前端使用 **snake_case**（如 `sort_order`）。

## Requirements

### Requirement: 账户列表

系统 SHALL 提供 `GET /api/v1/accounts` 返回当前用户的**活跃**账户列表，按 `sort_order` 排序。

#### Scenario: 获取账户列表

- **WHEN** `GET /api/v1/accounts`
- **THEN** 返回 200 和 `{"accounts": [{"id", "name", "type", "owner", "sort_order", "is_active", ...}]}`

### Requirement: 创建账户

系统 SHALL 提供 `POST /api/v1/accounts` 创建新账户。

#### Scenario: 创建成功

- **WHEN** `POST /api/v1/accounts` 且 `{"name", "type", "owner"}` 合法
- **THEN** 返回 201 和 `{"account": {...}}`，`sort_order` 自动置于末尾

### Requirement: 更新账户

系统 SHALL 提供 `PUT /api/v1/accounts/{id}` 更新账户。

#### Scenario: 更新成功

- **WHEN** `PUT /api/v1/accounts/{id}` 且 `name` 非空、`type`/`owner` 合法
- **THEN** 返回 200 和 `{"ok": true}`

#### Scenario: 账户不存在

- **WHEN** id 不属于当前用户
- **THEN** 返回 404，`{"error": "账户不存在"}`

### Requirement: 停用账户

系统 SHALL 提供 `DELETE /api/v1/accounts/{id}` 将账户标记为非活跃（软删除）。

#### Scenario: 停用成功

- **WHEN** `DELETE /api/v1/accounts/{id}`
- **THEN** 返回 200 和 `{"ok": true}`

### Requirement: 账户排序

系统 SHALL 提供 `PUT /api/v1/accounts/reorder`，请求体 `{"accountIds": ["id1", "id2", ...]}`。

#### Scenario: 排序成功

- **WHEN** 数组非空且 id 均为当前用户活跃账户
- **THEN** 返回 200 和 `{"ok": true}`，顺序与数组一致

### Requirement: 校验失败

- **WHEN** `name` 为空，或 `type`/`owner` 不是当前用户已启用的选项 key
- **THEN** 返回 400 和 `{"error": "参数不合法"}`
