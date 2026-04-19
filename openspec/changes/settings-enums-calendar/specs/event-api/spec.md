## MODIFIED Requirements

### Requirement: 大事记分类

category SHALL 为用户设置中 `event_category` 维度下已启用选项的 `key` 之一；系统 SHALL NOT 使用与设置无关的固定枚举集合进行校验。

#### Scenario: 分类标签

- **WHEN** 前端显示分类或 API 返回 `byCategory[].category`
- **THEN** SHALL 使用设置中的 `label` 映射展示；`category` 字段值 SHALL 为配置中的 `key`

### Requirement: 金额取绝对值

统计时，支出类统计 total SHALL 为该分类下所有**支出**大事记金额的绝对值之和，其中支出 SHALL 由负数金额表示；若某分类仅有收入（正数金额），则该分类在支出统计中 total SHALL 为 0 或 SHALL 不出现在支出汇总中（行为 SHALL 在实现中保持一致并写入 API 说明）。

#### Scenario: 金额处理

- **WHEN** 两条 travel 大事记金额为 -5000 和 -10000
- **THEN** byCategory 中 travel.total = 15000

#### Scenario: 存在正数收入

- **WHEN** 某分类存在正数金额的大事记
- **THEN** 支出合计 SHALL NOT 将正数金额误加为支出
