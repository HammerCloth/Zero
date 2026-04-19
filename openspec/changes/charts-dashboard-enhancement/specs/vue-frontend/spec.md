## ADDED Requirements

### Requirement: 仪表盘图表组件

前端 SHALL 在仪表盘相关视图中注册并使用 ECharts 所需图表类型（含堆叠面积、多折线等）；SHALL 复用 `settingsStore` 将构成与大事记图表中的 key 转为标签。

#### Scenario: 按需引入

- **WHEN** 构建生产包
- **THEN** SHALL 沿用 tree-shaking 按需注册 ECharts 模块，避免无谓全量引入

#### Scenario: 标签一致

- **WHEN** 渲染资产构成或大事记图表
- **THEN** 展示文本 SHALL 与设置中的 `label` 一致（未知 key 降级）
