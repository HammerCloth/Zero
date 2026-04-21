## ADDED Requirements

### Requirement: 按类型分账户的构成数据

系统 SHALL 在现有 `GET /api/v1/dashboard/composition` 响应中增加字段，或提供新的只读端点 `GET /api/v1/dashboard/composition-accounts`，用于返回**最新快照**下：每个 **账户类型** 内各 **账户** 的金额（与 `composition` 现有 `byType` 聚合口径一致，负债类型符号与 `BalanceLogic` / 现有构成实现一致）。响应 SHALL 已认证且按当前 `user_id` 隔离。

#### Scenario: 扩展 composition 响应

- **WHEN** `GET /api/v1/dashboard/composition`
- **THEN** 返回 200，且在保留 `byType` 与 `byOwner` 的前提下包含结构化字段（例如 `byTypeAccounts`: 类型 key → 账户 id → 金额），供前端绘制「类型内账户占比」

#### Scenario: 或独立端点

- **WHEN** `GET /api/v1/dashboard/composition-accounts`（若采用独立端点方案）
- **THEN** 返回 200 与上述等价结构，且与 `composition` 使用同一快照版本

#### Scenario: 无快照

- **WHEN** 用户无快照
- **THEN** 返回 200，`byType` / `byOwner` 为空对象或等价，新增结构为空映射或空数组且不报错
