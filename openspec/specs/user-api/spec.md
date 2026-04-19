# user-api Specification

## Purpose

管理员管理其它登录用户，前缀 **`/api/v1/users`**。普通用户无权限调用；用户 JSON 字段见 `User`（如 `is_admin`、`must_change_password`、`created_at`）。

## Requirements

### Requirement: 用户列表

系统 SHALL 提供 `GET /api/v1/users` 返回所有用户列表，**仅管理员**可访问。

#### Scenario: 管理员获取列表

- **WHEN** `GET /api/v1/users` 由管理员调用
- **THEN** 返回 200 和 `{"users": [...]}`

#### Scenario: 非管理员拒绝

- **WHEN** `GET /api/v1/users` 由非管理员调用
- **THEN** 返回 403 和 `{"error": "无权限执行此操作"}`

### Requirement: 创建用户

系统 SHALL 提供 `POST /api/v1/users` 创建新用户，仅管理员可访问；新用户默认 `must_change_password=true`。

#### Scenario: 创建成功

- **WHEN** `POST /api/v1/users` `{"username", "password", "is_admin"}` 由管理员调用
- **THEN** 返回 201 和 `{"user": {...}}`

#### Scenario: 用户名重复

- **WHEN** 用户名已存在
- **THEN** 返回 409 和 `{"error": "用户名已存在"}`

#### Scenario: 密码过短

- **WHEN** 密码少于 8 位
- **THEN** 返回 400 和 `{"error": "密码至少 8 位"}`

### Requirement: 重置用户密码

系统 SHALL 提供 `PUT /api/v1/users/{id}/password` 由管理员重置指定用户密码，目标用户 `must_change_password` 设为 true。

#### Scenario: 重置成功

- **WHEN** `PUT /api/v1/users/{id}/password` `{"password": "..."}` 由管理员调用
- **THEN** 返回 200 和 `{"ok": true}`

#### Scenario: 用户不存在

- **WHEN** id 不存在
- **THEN** 返回 404 和 `{"error": "用户不存在"}`
