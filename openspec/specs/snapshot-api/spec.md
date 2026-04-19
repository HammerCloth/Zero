# snapshot-api Specification

## Purpose

快照 CRUD 与按日查询，前缀 **`/api/v1/snapshots`**。数据按当前登录用户 **`user_id`** 隔离；快照 ID 为 32 位十六进制字符串。
## Requirements
### Requirement: 快照列表
系统 SHALL 提供 GET /api/v1/snapshots 端点返回当前用户的所有快照，按日期倒序。

#### Scenario: 获取快照列表
- **WHEN** GET /api/v1/snapshots
- **THEN** 返回 200 和 `{"snapshots": [{"id": "...", "date": "2024-01-01", "createdAt": "...", "netWorth": 100000}]}`

### Requirement: 获取单个快照
系统 SHALL 提供 GET /api/v1/snapshots/{id} 端点返回快照详情，包含所有明细项和大事记。

#### Scenario: 获取成功
- **WHEN** GET /api/v1/snapshots/123
- **THEN** 返回 200 和 `{"snapshot": {"id": "...", "date": "...", "items": [...], "events": [...]}}`

#### Scenario: 快照不存在
- **WHEN** GET /api/v1/snapshots/999
- **THEN** 返回 404 和 `{"error": "快照不存在"}`

### Requirement: 获取最新快照
系统 SHALL 提供 GET /api/v1/snapshots/latest 端点返回最新一条快照。

#### Scenario: 有快照时
- **WHEN** GET /api/v1/snapshots/latest 用户有快照
- **THEN** 返回 200 和 `{"snapshot": {...}}`

#### Scenario: 无快照时
- **WHEN** GET /api/v1/snapshots/latest 用户无快照
- **THEN** 返回 200 和 `{"snapshot": null}`

### Requirement: 按日期查询与日历范围

系统 SHALL 提供 `GET /api/v1/snapshots/for-date?date=YYYY-MM-DD` 返回该日是否已有快照及详情或空壳；以及 `GET /api/v1/snapshots/dates-in-range?from=...&to=...` 返回区间内已有快照的日期列表（供日历视图使用）。

### Requirement: 创建快照
系统 SHALL 提供 POST /api/v1/snapshots 端点创建快照及其明细和大事记。

#### Scenario: 创建成功
- **WHEN** POST /api/v1/snapshots `{"date": "2024-01-01", "items": [{"accountId": "...", "balance": 10000}], "events": [{"category": "travel", "description": "日本旅行", "amount": -5000}]}`
- **THEN** 返回 201 和 `{"snapshot": {...}}`

#### Scenario: 余额未填
- **WHEN** POST /api/v1/snapshots 某账户 balance 为 null
- **THEN** 返回 400 和 `{"error": "存在未填写余额的账户"}`

### Requirement: 更新快照
系统 SHALL 提供 PUT /api/v1/snapshots/{id} 端点更新快照及其明细和大事记。

#### Scenario: 更新成功
- **WHEN** PUT /api/v1/snapshots/123 `{"date": "2024-01-02", "items": [...], "events": [...]}`
- **THEN** 返回 200 和 `{"snapshot": {...}}`

### Requirement: 删除快照
系统 SHALL 提供 DELETE /api/v1/snapshots/{id} 端点删除快照及其关联数据。

#### Scenario: 删除成功
- **WHEN** DELETE /api/v1/snapshots/123
- **THEN** 返回 200 和 `{"ok": true}`

### Requirement: 快照明细项
每个快照 SHALL 包含多个明细项 items，每项 SHALL 包含 accountId 和 balance。balance 正数 SHALL 表示资产，负数 SHALL 表示负债。

#### Scenario: 负债账户余额处理
- **WHEN** 账户类型为 credit 且用户输入正数余额
- **THEN** 系统自动转为负数存储

### Requirement: 大事记
每个快照 MAY 包含多个大事记 events；每项 SHALL 包含 category（分类）、description（描述）、amount（金额，负数 SHALL 表示支出）。

#### Scenario: 大事记分类
- **WHEN** 创建大事记
- **THEN** category SHALL 为 rent/travel/medical/appliance/social/other 之一

