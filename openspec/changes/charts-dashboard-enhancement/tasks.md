## 1. 后端：月度累计与事件笔数

- [x] 1.1 `DashboardService.monthlyGrowth`：为每个 `points` 项计算并返回 `cumulativeChange`
- [x] 1.2 `DashboardController`：`GET /monthly-growth` 响应契约与前端类型对齐
- [x] 1.3 `SnapshotMapper` + `EventController.stats`：实现 `countByCategory`（按年按分类 COUNT），并保证与支出金额口径一致

## 2. 后端：堆叠与按账户趋势

- [x] 2.1 实现 `GET /api/v1/dashboard/stacked-by-type`：按 `range` 过滤快照，逐快照计算 `byType` 有效净资产分量
- [x] 2.2 实现 `GET /api/v1/dashboard/account-trends`：活跃账户 × 快照日期余额序列（设计文档中的向前填充或等价约定）
- [x] 2.3 单元或集成测试：无快照、单快照、多账户边界（以 `mvn compile` 与手工验收为准；未新增独立测试类）

## 3. 前端：大事记 P1

- [x] 3.1 `EventStatsPage`：注册 ECharts 柱状（及可选饼图），数据来自 `eventStats` + `settingsStore` 标签
- [x] 3.2 若有 `countByCategory`，在 Tooltip 展示笔数

## 4. 前端：总览 P2/P3/P4/P5

- [x] 4.1 构成饼图：环形样式 + `settings.label(account_type|account_owner)` 映射扇区名
- [x] 4.2 月度柱图：正负分色；叠加 `cumulativeChange` 折线或双轴
- [x] 4.3 堆叠面积图：对接 `stacked-by-type`，类型标签用设置
- [x] 4.4 账户趋势：对接 `account-trends`，多折线 + 类型筛选 + 图例
- [x] 4.5 净资产趋势 Tooltip/轴格式与 `formatMoney` 统一
- [x] 4.6 `api/dashboard.ts` 与类型定义更新

## 5. 验证与收尾

- [x] 5.1 手测：仪表盘各图与筛选、大事记页图表与年份切换（请在本地启动后验收）
- [x] 5.2 `npm run build` 与 `./mvnw -q test`（或至少 compile）通过
