## 1. 后端：构成 API 扩展

- [x] 1.1 在 `DashboardService` 中基于最新快照计算「账户类型 → 账户 id → 金额」映射，口径与现有 `composition` / `BalanceLogic` 一致
- [x] 1.2 在 `GET /api/v1/dashboard/composition` 响应中增加字段（推荐 `byTypeAccounts`），或新增 `GET /api/v1/dashboard/composition-accounts` 并在 `DashboardController` 暴露
- [x] 1.3 无快照时返回空结构；补充必要单元测试或集成测试（若项目已有 Dashboard 测试模式）

## 2. 前端：构建体积与拆包

- [x] 2.1 移除 `frontend-vue/src/main.ts` 中 `app.use(naive)` 全量注册，确认 `unplugin-vue-components` + `NaiveUiResolver` 覆盖所用 `n-*` 组件；按需保留官方全局样式 import
- [x] 2.2 在 `frontend-vue/vite.config.ts` 配置 `build.rollupOptions.output.manualChunks`，将 `echarts` / `zrender` / `vue-echarts` 拆为独立 vendor chunk
- [x] 2.3 执行 `npm run build`，确认产物体积与 chunk 分布改善且无运行时报错（`vite preview` 手测登录与仪表盘）

## 3. 前端：仪表盘布局与构成图

- [x] 3.1 将 `DashboardPage.vue` 中资产构成两段由固定 `n-grid :cols="2"` 改为响应式（小屏单列，如 `n-grid` `responsive="screen"` 与 `span` 配置，或与项目其它卡片一致之断点）
- [x] 3.2 为 `typePie` / `ownerPie`（及必要时其它饼图）增加 ECharts `legend`（如 `type: 'scroll'`）或 `label` 布局，避免小屏裁切
- [x] 3.3 在 `frontend-vue/src/api/dashboard.ts` 中为 `fetchComposition`（或新函数）增加 TypeScript 类型，消费 `byTypeAccounts`（或独立端点返回类型）

## 4. 前端：类型内账户占比图

- [x] 4.1 根据 `byTypeAccounts` 构造 ECharts 选项（推荐：**每个类型一条 100% 堆叠条**或 **分面小 multiples**，择一在实现中固定并在 PR 说明）
- [x] 4.2 在 `DashboardPage.vue` 增加卡片与 `v-chart`，账户名 / 类型标签使用 `settingsStore.label`
- [x] 4.3 空数据与单账户类型的边界展示；旧后端无新字段时的降级（隐藏新卡或空状态）

## 5. 验收

- [x] 5.1 对照 `openspec/changes/vue-perf-dashboard-ux/specs/` 下 delta 与 `design.md`，走查移动端与桌面端 `/dashboard`
- [x] 5.2 DevTools Network 确认首屏主 JS 传输体积或 chunk 数量相对变更前可接受，且无多余全量 Naive 打入入口之迹象（可用 rollup 可视化或 `vite build --debug` 辅助）
