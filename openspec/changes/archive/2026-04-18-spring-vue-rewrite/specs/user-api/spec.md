## ADDED Requirements

### Requirement: 用户列表
系统 SHALL 提供 GET /api/users 端点返回所有用户列表，仅管理员可访问。

#### Scenario: 管理员获取列表
- **WHEN** GET /api/users 由管理员调用
- **THEN** 返回 200 和 `{"users": [{"id": "...", "username": "...", "isAdmin": false, "createdAt": "..."}]}`

#### Scenario: 非管理员拒绝
- **WHEN** GET /api/users 由非管理员调用
- **THEN** 返回 403 和 `{"error": "无权限执行此操作"}`

### Requirement: 创建用户
系统 SHALL 提供 POST /api/users 端点创建新用户，仅管理员可访问。新用户默认 mustChangePassword=true。

#### Scenario: 创建成功
- **WHEN** POST /api/users `{"username": "user1", "password": "12345678", "isAdmin": false}` 由管理员调用
- **THEN** 返回 201 和 `{"user": {...}}`

#### Scenario: 用户名重复
- **WHEN** POST /api/users 用户名已存在
- **THEN** 返回 409 和 `{"error": "用户名已存在"}`

#### Scenario: 密码过短
- **WHEN** POST /api/users 密码少于 8 位
- **THEN** 返回 400 和 `{"error": "密码至少 8 位"}`

### Requirement: 重置用户密码
系统 SHALL 提供 PUT /api/users/{id}/password 端点由管理员重置指定用户密码，目标用户 mustChangePassword 设为 true。

#### Scenario: 重置成功
- **WHEN** PUT /api/users/123/password `{"password": "newpass123"}` 由管理员调用
- **THEN** 返回 200 和 `{"ok": true}`，目标用户下次登录需修改密码

#### Scenario: 用户不存在
- **WHEN** PUT /api/users/999/password 用户 ID 不存在
- **THEN** 返回 404 和 `{"error": "用户不存在"}`
