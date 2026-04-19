# dashboard-api Specification

## Purpose

定义当前用户净资产仪表盘所需的只读 JSON API（Spring Boot，`/api/v1/dashboard`）。所有端点需已认证；数据按 **当前登录用户的 `user_id`** 隔离。

## Requirements

### Requirement: 汇总数据

系统 SHALL 提供 `GET /api/v1/dashboard/summary` 返回当前用户的资产汇总。

#### Scenario: 获取汇总

- **WHEN** `GET /api/v1/dashboard/summary`
- **THEN** 返回 200 和 `{"netWorth": number, "monthlyChange": number, "annualChange": number, "annualizedReturn": number}`

#### Scenario: 无快照时

- **WHEN** 用户无快照
- **THEN** 返回 200 且各数值为 0（或等价约定）

### Requirement: 趋势数据

系统 SHALL 提供 `GET /api/v1/dashboard/trend` 返回净资产趋势点。

#### Scenario: 范围参数

- **WHEN** `GET /api/v1/dashboard/trend?range=1y`（或 `3m` / `6m` / `all`）
- **THEN** 返回 200 和 `{"points": [{"date": "YYYY-MM-DD", "netWorth": number}]}`

### Requirement: 资产构成

系统 SHALL 提供 `GET /api/v1/dashboard/composition` 返回最新快照上按类型与归属的分布。

#### Scenario: 获取构成

- **WHEN** `GET /api/v1/dashboard/composition`
- **THEN** 返回 200 和 `{"byType": {...}, "byOwner": {...}}`（值为金额）

### Requirement: 月度增长

系统 SHALL 提供 `GET /api/v1/dashboard/monthly-growth` 返回指定年份每月净资产变化。

#### Scenario: 当年与累计

- **WHEN** `GET /api/v1/dashboard/monthly-growth` 或 `?year=2024`
- **THEN** 返回 200 和 `{"year": number, "points": [{"month": "01"|...|"12", "change": number, "cumulativeChange": number}]}`，其中 `cumulativeChange` 为该年 1 月起至该月的 `change` 累加（含当月）

### Requirement: 按类型堆叠趋势

系统 SHALL 提供 `GET /api/v1/dashboard/stacked-by-type`，在指定 `range` 内对每个快照日期返回按**账户类型**拆分的净资产分量，供堆叠面积图使用。

#### Scenario: 有快照

- **WHEN** 用户有快照且 `range` 合法
- **THEN** 返回 200 和 `{"points": [{"date": "YYYY-MM-DD", "byType": {"cash": number, ...}}]}`

#### Scenario: 无快照

- **WHEN** 用户无快照
- **THEN** 返回 200 且 `points` 为空数组

### Requirement: 按账户余额趋势

系统 SHALL 提供 `GET /api/v1/dashboard/account-trends`，返回每个**活跃**账户在指定 `range` 下的余额时间序列。

#### Scenario: 多账户

- **WHEN** 存在多个活跃账户与快照
- **THEN** 返回 200 和 `{"accounts": [{"accountId", "name", "type", "points": [{"date", "balance"}]}]}`（停用账户默认不包含）

### Requirement: 净资产与负债口径

负债类账户（如 `credit`）在汇总与分量计算中 SHALL 按业务约定的符号参与净资产（与 `BalanceLogic` 一致）。

## Notes

- `range` 语义与 `trend` 一致：`3m` / `6m` / `1y` / `all`（缺省行为以实现为准）。
