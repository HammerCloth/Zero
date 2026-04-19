## ADDED Requirements

### Requirement: 检查初始化状态
系统 SHALL 提供 GET /api/auth/status 端点返回系统是否需要初始化（无用户时需要）。

#### Scenario: 系统未初始化
- **WHEN** GET /api/auth/status 且数据库无用户
- **THEN** 返回 200 和 `{"needsSetup": true}`

#### Scenario: 系统已初始化
- **WHEN** GET /api/auth/status 且数据库有用户
- **THEN** 返回 200 和 `{"needsSetup": false}`

### Requirement: 初始化管理员
系统 SHALL 提供 POST /api/auth/setup 端点创建第一个管理员用户，仅在无用户时可用。

#### Scenario: 初始化成功
- **WHEN** POST /api/auth/setup `{"username": "admin", "password": "12345678"}` 且数据库无用户
- **THEN** 创建管理员用户，返回 200 和 `{"user": {...}, "accessToken": "..."}`，设置 refresh_token Cookie

#### Scenario: 重复初始化
- **WHEN** POST /api/auth/setup 且数据库已有用户
- **THEN** 返回 409 和 `{"error": "初始化已完成"}`

#### Scenario: 密码过短
- **WHEN** POST /api/auth/setup 密码少于 8 位
- **THEN** 返回 400 和 `{"error": "密码至少 8 位"}`

### Requirement: 用户登录
系统 SHALL 提供 POST /api/auth/login 端点验证用户名密码并颁发令牌。

#### Scenario: 登录成功
- **WHEN** POST /api/auth/login `{"username": "admin", "password": "correct", "rememberMe": true}`
- **THEN** 返回 200 和 `{"user": {...}, "accessToken": "..."}`，设置 refresh_token Cookie（记住我时有效期 30 天，否则 1 天）

#### Scenario: 密码错误
- **WHEN** POST /api/auth/login 密码不正确
- **THEN** 返回 401 和 `{"error": "用户名或密码错误"}`

#### Scenario: 用户不存在
- **WHEN** POST /api/auth/login 用户名不存在
- **THEN** 返回 401 和 `{"error": "用户名或密码错误"}`（不泄露用户是否存在）

### Requirement: 刷新令牌
系统 SHALL 提供 POST /api/auth/refresh 端点使用 refresh_token Cookie 颁发新的 access_token。

#### Scenario: 刷新成功
- **WHEN** POST /api/auth/refresh 携带有效的 refresh_token Cookie
- **THEN** 返回 200 和 `{"user": {...}, "accessToken": "..."}`，更新 Cookie

#### Scenario: 刷新失败
- **WHEN** POST /api/auth/refresh 无 Cookie 或 Cookie 无效
- **THEN** 返回 401 和 `{"error": "未登录或登录已过期"}`

### Requirement: 登出
系统 SHALL 提供 POST /api/auth/logout 端点清除 refresh_token Cookie。

#### Scenario: 登出成功
- **WHEN** POST /api/auth/logout
- **THEN** 返回 200 和 `{"ok": true}`，清除 Cookie

### Requirement: 获取当前用户
系统 SHALL 提供 GET /api/auth/me 端点返回当前登录用户信息，需要认证。

#### Scenario: 获取成功
- **WHEN** GET /api/auth/me 携带有效 access_token
- **THEN** 返回 200 和 `{"id": "...", "username": "...", "isAdmin": true, "mustChangePassword": false}`

#### Scenario: 未认证
- **WHEN** GET /api/auth/me 无 access_token
- **THEN** 返回 401 和 `{"error": "未登录或登录已过期"}`

### Requirement: 修改密码
系统 SHALL 提供 PUT /api/auth/password 端点允许用户修改自己的密码，需要认证。

#### Scenario: 修改成功
- **WHEN** PUT /api/auth/password `{"currentPassword": "old", "newPassword": "new12345"}` 当前密码正确
- **THEN** 返回 200 和 `{"ok": true}`，mustChangePassword 标记清除

#### Scenario: 当前密码错误
- **WHEN** PUT /api/auth/password 当前密码不正确
- **THEN** 返回 400 和 `{"error": "当前密码错误"}`

### Requirement: JWT 令牌格式
access_token 和 refresh_token SHALL 均为 JWT，并 SHALL 包含 sub（用户 ID）和 exp（过期时间）。access_token 有效期 SHALL 为 1 小时，refresh_token 有效期 SHALL 取决于 rememberMe。

#### Scenario: 令牌过期拒绝
- **WHEN** 使用过期的 access_token 访问受保护资源
- **THEN** 返回 401

### Requirement: Cookie 安全属性
refresh_token Cookie SHALL 设置 HttpOnly=true、Secure=true（生产环境）、SameSite=Strict、Path=/api/auth。

#### Scenario: Cookie 属性正确
- **WHEN** 登录成功
- **THEN** Set-Cookie 头包含 HttpOnly; Secure; SameSite=Strict; Path=/api/auth

### Requirement: 登录频率限制
系统 SHALL 限制单 IP 登录失败次数，5 次失败后锁定 15 分钟。

#### Scenario: 触发锁定
- **WHEN** 同一 IP 连续 5 次登录失败
- **THEN** 返回 429 和 `{"error": "登录尝试次数过多，请 15 分钟后再试"}`
