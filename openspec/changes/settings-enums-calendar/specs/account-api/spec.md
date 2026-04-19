## MODIFIED Requirements

### Requirement: 账户类型

账户类型 type SHALL 为用户设置中 `account_type` 维度下已启用选项的 `key` 之一；系统 SHALL NOT 再使用与设置无关的固定枚举集合进行校验。

#### Scenario: 类型校验

- **WHEN** POST 或 PUT /api/accounts 且 type 不是当前用户设置中启用的 key
- **THEN** 返回 400 和 `{"error": "参数不合法"}` 或更明确的校验信息

#### Scenario: 合法类型

- **WHEN** type 与设置中某启用项的 key 一致
- **THEN** 请求 SHALL 通过类型校验

### Requirement: 账户归属

账户归属 owner SHALL 为用户设置中 `account_owner` 维度下已启用选项的 `key` 之一；系统 SHALL NOT 再使用与设置无关的固定枚举集合进行校验。

#### Scenario: 归属校验

- **WHEN** POST 或 PUT /api/accounts 且 owner 不是当前用户设置中启用的 key
- **THEN** 返回 400 和 `{"error": "参数不合法"}` 或更明确的校验信息

#### Scenario: 合法归属

- **WHEN** owner 与设置中某启用项的 key 一致
- **THEN** 请求 SHALL 通过归属校验
