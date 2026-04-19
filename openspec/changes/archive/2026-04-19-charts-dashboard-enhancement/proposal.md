## Why

仪表盘与大事记相关能力已可用，但「可读性」与「分析深度」仍不足：大事记统计以表格为主、总览图表在标签与样式上未与设置中心对齐、月度变化缺少累计视角、资产随时间按类型拆分与按账户趋势尚未在前后端贯通。本变更一次性落实此前梳理的 **P1～P5**，在可维护前提下提升数据可视化与决策辅助价值。

## What Changes

- **P1（大事记可视化）**：在 `/events` 统计页增加至少一种图表（如柱状图）与可选饼图，与现有表格、合计共存；分类轴使用设置中的中文标签。
- **P2（总览图表样式）**：饼图改为环形或等价更易读样式；月度柱图对正负变化分色；坐标轴与提示框货币格式统一；趋势图 Tooltip/图例可配置。
- **P3（构成图标签）**：资产构成饼图（类型/归属）展示数据 SHALL 使用 `settings` 中 `label` 映射展示，而非裸 key。
- **P4（月度增长增强）**：在指定年「每月净资产变化」基础上增加 **累计变化**（自当年 1 月起至该月的累计），或等价第二条序列（与 `design.md` 口径一致）；可选展示与去年同期对比（若实现则需在规格中定义口径）。
- **P5（按账户趋势）**：新增后端聚合接口，返回各活跃账户在选定时间范围内的余额时间序列；前端在仪表盘提供多序列折线图（或账户多选），并支持按类型筛选账户（与现有 `dashboard-page` 中「账户明细趋势」意向对齐）。

**BREAKING**

- 无破坏性 API 删除；若新增响应字段，旧客户端 SHALL 忽略未知字段。

## Capabilities

### New Capabilities

（无独立新能力目录；功能落在现有 dashboard / event / vue 能力下。）

### Modified Capabilities

- `dashboard-api`：新增堆叠/按类型时间序列、按账户余额趋势、扩展月度增长响应（累计等）；路径以现有 **`/api/v1/dashboard`** 为准。
- `dashboard-page`：图表样式与交互、构成标签、月度双序列、堆叠面积图、账户趋势图、加载与空状态。
- `event-api`：若需为图表补充笔数等字段，在 `stats` 响应中扩展（可选）。
- `event-page`：统计页图表与布局。
- `vue-frontend`：ECharts 模块注册、设置 store 在图表中的复用。

## Impact

**后端**：`DashboardService` 扩展；`DashboardController` 新增端点；`SnapshotMapper` 可能需辅助查询；`EventController` 可选扩展 `stats` 结构。

**前端**：`DashboardPage.vue`、`EventStatsPage.vue` 增强；`api/dashboard.ts` 与类型；按需引入 ECharts 组件（如 `BarChart` 已存在，可能增加 `StackedBar`/`Line` 等）。

**性能**：按账户趋势在多账户、长快照历史下需注意点数；设计阶段约定最大账户数或下采样策略。
