## MODIFIED Requirements

### Requirement: 资产构成图

仪表盘 SHALL 显示两个饼图：按类型分布、按归属分布；图例与扇区标签 SHALL 使用设置中对应维度的中文 `label`（`account_type` / `account_owner`），未知 key SHALL 显示 key 或占位文案。

#### Scenario: 类型分布

- **WHEN** 查看「资产构成（类型）」
- **THEN** 显示饼图（推荐环形图），扇区名称 SHALL 为设置中的标签而非裸英文 key

#### Scenario: 归属分布

- **WHEN** 查看「资产构成（归属）」
- **THEN** 显示饼图（推荐环形图），扇区名称 SHALL 为设置中的标签

### Requirement: 月度增长图

仪表盘 SHALL 显示当年每月净资产变化的柱状图，并 SHALL 在同一卡片内或紧邻区域展示**累计变化**折线或第二 Y 轴序列（与 `cumulativeChange` 对齐）；柱图对正 `change` 与负 `change` SHALL 使用可区分颜色。

#### Scenario: 月度数据

- **WHEN** 查看「月度净资产变化」
- **THEN** 显示 1–12 月数据，且能读取累计序列

#### Scenario: 正负分色

- **WHEN** 某月 change 为负
- **THEN** 该月柱形颜色 SHALL 与正增长区分

### Requirement: 资产堆叠图

仪表盘 SHALL 显示按资产类型堆叠的面积图，展示各类资产随快照日期的历史变化；数据 SHALL 来自 `/api/v1/dashboard/stacked-by-type`（或等价端点），并与时间范围选择器与总净资产趋势范围选项一致或提供独立 `range` 控件。

#### Scenario: 堆叠展示

- **WHEN** 查看「资产堆叠（按类型）」
- **THEN** 显示堆叠面积图，各层类型名称 SHALL 使用设置中的中文标签

### Requirement: 账户明细趋势

仪表盘 SHALL 显示各账户余额趋势图；SHALL 调用 `/api/v1/dashboard/account-trends` 获取数据；SHALL 提供按账户类型筛选（过滤展示的账户序列）；多账户时 SHALL 使用多折线或可切换图例。

#### Scenario: 筛选类型

- **WHEN** 选择「基金」筛选
- **THEN** 只显示类型为基金相关账户的曲线（或等价过滤行为）

#### Scenario: 空数据

- **WHEN** 无快照或单账户无点
- **THEN** 显示空状态而非报错

### Requirement: 净资产趋势图

仪表盘 SHALL 显示净资产趋势折线图，支持时间范围切换（3 个月/6 个月/1 年/全部）；Tooltip 与坐标轴数值 SHALL 使用统一货币格式（与 `formatMoney` 或项目约定一致）。

#### Scenario: 范围切换

- **WHEN** 点击「1年」按钮
- **THEN** 图表只显示最近一年的数据
