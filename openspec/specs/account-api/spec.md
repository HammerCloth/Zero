# account-api Specification

## Purpose
TBD - created by archiving change spring-vue-rewrite. Update Purpose after archive.
## Requirements
### Requirement: 账户列表
系统 SHALL 提供 GET /api/accounts 端点返回当前用户的所有账户，按 sortOrder 排序。

#### Scenario: 获取账户列表
- **WHEN** GET /api/accounts
- **THEN** 返回 200 和 `{"accounts": [{"id": "...", "name": "...", "type": "cash", "owner": "A", "sortOrder": 1, "isActive": true}]}`

### Requirement: 创建账户
系统 SHALL 提供 POST /api/accounts 端点创建新账户。

#### Scenario: 创建成功
- **WHEN** POST /api/accounts `{"name": "工商银行", "type": "deposit", "owner": "A"}`
- **THEN** 返回 201 和 `{"account": {...}}`，sortOrder 自动递增

### Requirement: 更新账户
系统 SHALL 提供 PUT /api/accounts/{id} 端点更新账户信息。

#### Scenario: 更新成功
- **WHEN** PUT /api/accounts/123 `{"name": "新名称", "type": "fund", "owner": "B"}`
- **THEN** 返回 200 和 `{"ok": true}`

#### Scenario: 账户不存在
- **WHEN** PUT /api/accounts/999
- **THEN** 返回 404 和 `{"error": "账户不存在"}`

### Requirement: 停用账户
系统 SHALL 提供 DELETE /api/accounts/{id} 端点将账户标记为 isActive=false（软删除）。

#### Scenario: 停用成功
- **WHEN** DELETE /api/accounts/123
- **THEN** 返回 200 和 `{"ok": true}`，账户 isActive 变为 false

### Requirement: 账户排序
系统 SHALL 提供 PUT /api/accounts/reorder 端点批量更新账户排序。

#### Scenario: 排序成功
- **WHEN** PUT /api/accounts/reorder `{"accountIds": ["id1", "id2", "id3"]}`
- **THEN** 返回 200 和 `{"ok": true}`，账户按数组顺序更新 sortOrder

### Requirement: 账户类型
账户类型 type SHALL 为以下枚举值之一：cash（现金）、deposit（固收）、fund（基金）、pension（养老）、housing_fund（公积金）、credit（负债）。

#### Scenario: 类型校验
- **WHEN** POST /api/accounts type 不是有效枚举值
- **THEN** 返回 400 和 `{"error": "参数不合法"}`

### Requirement: 账户归属
账户归属 owner SHALL 为以下枚举值之一：A、B、shared。

#### Scenario: 归属校验
- **WHEN** POST /api/accounts owner 不是有效枚举值
- **THEN** 返回 400 和 `{"error": "参数不合法"}`

