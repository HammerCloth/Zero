## ADDED Requirements

### Requirement: 按日期查询快照

系统 SHALL 提供 GET 端点，使客户端能够用 `date=YYYY-MM-DD` 查询当前用户在该日是否已有快照，并返回足够用于路由跳转的标识（例如快照 `id` 或空）。

#### Scenario: 当日有快照

- **WHEN** GET `/api/snapshots/for-date?date=2024-06-01` 且该日已有快照
- **THEN** 返回 200 且响应体 SHALL 包含该快照的 `id` 或等价引用

#### Scenario: 当日无快照

- **WHEN** GET `/api/snapshots/for-date?date=2024-06-01` 且该日无快照
- **THEN** 返回 200 且响应体 SHALL 明确表示无快照（例如 `snapshot: null`）

#### Scenario: 日期非法

- **WHEN** `date` 非合法 `YYYY-MM-DD`
- **THEN** 返回 400

## MODIFIED Requirements

### Requirement: 大事记

每个快照 MAY 包含多个大事记 events；每项 SHALL 包含 category（分类 key）、description（描述）、amount（有符号金额）。负数 SHALL 表示支出，正数 SHALL 表示收入；系统 SHALL 在校验 category 时使用用户设置中 `event_category` 维度下已启用的 key。

#### Scenario: 大事记分类合法

- **WHEN** 创建或更新大事记
- **THEN** category SHALL 为当前用户设置中启用的 `event_category` key 之一

#### Scenario: 收支符号

- **WHEN** 存储或返回大事记金额
- **THEN** 正数 SHALL 表示收入，负数 SHALL 表示支出，且 SHALL 与统计与展示规则一致
