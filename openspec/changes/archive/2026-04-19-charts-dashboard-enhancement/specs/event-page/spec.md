## ADDED Requirements

### Requirement: 大事记统计图表

大事记统计页 SHALL 在表格之外提供至少一种图表展示当年按分类的支出合计（例如柱状图），并 MAY 提供饼图展示占比；图表横轴/图例 SHALL 使用设置中 `event_category` 的中文标签。

#### Scenario: 与表格并存

- **WHEN** 访问统计页
- **THEN** 同时显示数据表格与图表，且合计与 `grandTotal` 一致

#### Scenario: 切换年份

- **WHEN** 修改年份
- **THEN** 图表 SHALL 与表格一并更新

## MODIFIED Requirements

### Requirement: 分类标签

分类 SHALL 显示为当前用户在设置中为 `event_category` 配置的 `label`；若某历史 `key` 在设置中已删除或禁用，页面 SHALL 仍显示合理降级（例如显示 key 或「未知」）。

#### Scenario: 标签映射

- **WHEN** 统计行中的 category key 在设置中存在
- **THEN** 显示为该 key 对应的 `label`

#### Scenario: 未知 key

- **WHEN** category key 在设置中不存在
- **THEN** 页面 SHALL 显示可辨识的占位文案或原始 key
