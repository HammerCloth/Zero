# dashboard-api Specification

## Purpose
TBD - created by archiving change spring-vue-rewrite. Update Purpose after archive.
## Requirements
### Requirement: 汇总数据
系统 SHALL 提供 GET /api/dashboard/summary 端点返回当前用户的资产汇总。

#### Scenario: 获取汇总
- **WHEN** GET /api/dashboard/summary
- **THEN** 返回 200 和 `{"netWorth": 100000, "monthlyChange": 5000, "annualChange": 20000, "annualizedReturn": 0.12}`

#### Scenario: 无快照时
- **WHEN** GET /api/dashboard/summary 用户无快照
- **THEN** 返回 200 和 `{"netWorth": 0, "monthlyChange": 0, "annualChange": 0, "annualizedReturn": 0}`

### Requirement: 趋势数据
系统 SHALL 提供 GET /api/dashboard/trend 端点返回净资产趋势数据点。

#### Scenario: 默认全部数据
- **WHEN** GET /api/dashboard/trend
- **THEN** 返回 200 和 `{"points": [{"date": "2024-01-01", "netWorth": 100000}]}`

#### Scenario: 按范围筛选
- **WHEN** GET /api/dashboard/trend?range=1y
- **THEN** 返回最近一年的数据点

#### Scenario: 支持的范围
- **WHEN** range 参数
- **THEN** SHALL 支持 3m/6m/1y/all

### Requirement: 资产构成
系统 SHALL 提供 GET /api/dashboard/composition 端点返回资产按类型和归属的分布。

#### Scenario: 获取构成
- **WHEN** GET /api/dashboard/composition
- **THEN** 返回 200 和 `{"byType": {"cash": 10000, "fund": 50000}, "byOwner": {"A": 30000, "B": 30000}}`

### Requirement: 月度增长
系统 SHALL 提供 GET /api/dashboard/monthly-growth 端点返回指定年份每月的净资产变化。

#### Scenario: 获取当年
- **WHEN** GET /api/dashboard/monthly-growth
- **THEN** 返回 200 和 `{"year": 2024, "points": [{"month": "01", "change": 5000}]}`

#### Scenario: 指定年份
- **WHEN** GET /api/dashboard/monthly-growth?year=2023
- **THEN** 返回 2023 年的月度数据

### Requirement: 净资产计算
净资产 SHALL 计算为所有快照明细项 balance 之和，其中 credit 类型账户余额为负数。

#### Scenario: 计算示例
- **WHEN** 快照包含 cash:50000、credit:-10000
- **THEN** netWorth = 50000 + (-10000) = 40000

### Requirement: 年化收益率
年化收益率 SHALL 基于最早和最新快照计算，公式为 `(最新净资产/最早净资产)^(365/天数) - 1`。

#### Scenario: 不足一年
- **WHEN** 快照跨度 180 天，从 100000 增长到 110000
- **THEN** annualizedReturn ≈ 0.21（年化 21%）

