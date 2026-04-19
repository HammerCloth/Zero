## ADDED Requirements

### Requirement: 按类型堆叠趋势

系统 SHALL 提供 GET `/api/v1/dashboard/stacked-by-type`（或等价路径）端点，返回在指定 `range`（`3m`/`6m`/`1y`/`all`，与 `/trend` 语义一致）内，每个快照日期上按账户类型聚合的净资产分量，供前端绘制堆叠面积图。

#### Scenario: 有快照时返回序列

- **WHEN** GET `/api/v1/dashboard/stacked-by-type?range=1y` 且用户有快照
- **THEN** 返回 200 和 `{"points": [{"date": "2024-01-01", "byType": {"cash": 10000, "fund": 20000}}]}` 或等价结构，且各日期分量之和 SHALL 与该日总净资产（按现有 BalanceLogic）一致

#### Scenario: 无快照

- **WHEN** 用户无快照
- **THEN** 返回 200 且 `points` 为空数组

### Requirement: 按账户余额趋势

系统 SHALL 提供 GET `/api/v1/dashboard/account-trends` 端点，返回当前用户每个**活跃**账户在指定 `range` 下的余额时间序列。

#### Scenario: 返回多账户序列

- **WHEN** GET `/api/v1/dashboard/account-trends?range=1y` 且存在多个活跃账户与快照
- **THEN** 返回 200 和 `{"accounts": [{"accountId": "...", "name": "...", "type": "...", "points": [{"date": "...", "balance": 0}]}]}`，且每个 `points` 中的 `balance` SHALL 来自对应快照明细项

#### Scenario: 仅活跃账户

- **WHEN** 某账户已停用
- **THEN** 响应 SHALL NOT 包含该账户，或 SHALL 在规格允许的扩展字段中明确标记为历史（默认实现为不包含）

## MODIFIED Requirements

### Requirement: 月度增长

系统 SHALL 提供 GET `/api/v1/dashboard/monthly-growth` 端点返回指定年份每月的净资产变化；响应中每个 `points` 元素除 `month` 与 `change` 外，SHALL 包含 `cumulativeChange`，表示自该年 1 月起至该月的月度 `change` 之和（含当月）。

#### Scenario: 获取当年

- **WHEN** GET `/api/v1/dashboard/monthly-growth`
- **THEN** 返回 200 和 `{"year": 2024, "points": [{"month": "01", "change": 5000, "cumulativeChange": 5000}]}`

#### Scenario: 指定年份

- **WHEN** GET `/api/v1/dashboard/monthly-growth?year=2023`
- **THEN** 返回 2023 年的月度数据且含 `cumulativeChange`

#### Scenario: 累计定义

- **WHEN** 1 月 change=1000、2 月 change=2000
- **THEN** 1 月 cumulativeChange=1000，2 月 cumulativeChange=3000
