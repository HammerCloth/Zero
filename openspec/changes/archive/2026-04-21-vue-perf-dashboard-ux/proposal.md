## Why

生产环境（`mimixia.online`）下 Vue 首屏 **Content Download** 耗时明显，主因是 **JS 体积偏大**（如 `index-*.js`、`dist-*.js` 等大 chunk）；同时仪表盘在 **窄屏竖屏** 上两块资产构成图仍 **并排**，饼图过小且 **图例/标签可读性差**。此外用户需要 **按资产类型观察各账户在该类型内的占比**，当前仅有按类型汇总与按归属汇总，缺少「类型 → 账户」这一层的可视化。

## What Changes

- **前端性能**：缩小首屏与公共 vendor 下载体积；合理 **拆 chunk**；确保 **ECharts 等重依赖** 仅在需要图表的路由/组件路径加载；修正与规范文档不一致的 **Naive UI 全量 `app.use(naive)`** 用法（与「按需」一致）。
- **仪表盘移动端**：在宽度小于断点（与现有布局约定一致，如 `<768px`）时，资产构成相关区域 **单列堆叠**，饼图区域高度与 **图例/标签** 配置避免被裁切。
- **新可视化**：增加「**各资产类型下，各账户金额占比**」图表（基于**最新快照**口径，与现有构成数据一致），名称使用 `settingsStore` 维度标签。
- **API（推荐）**：新增或扩展只读 Dashboard API，返回按类型分组的账户分量，避免前端重复实现复杂聚合与口径偏差。

## Capabilities

### New Capabilities

（无独立新能力目录；行为归入既有 `dashboard` / `dashboard-page` / `dashboard-api` / `vue-frontend`。）

### Modified Capabilities

- `vue-frontend`：补充生产构建与按需加载的规范性要求（与当前实现缺口对齐）。
- `dashboard`：强化移动端构成图验收；新增「类型内账户占比」需求。
- `dashboard-page`：与上述仪表盘行为对齐的页面级场景。
- `dashboard-api`：新增（或扩展）只读 JSON 字段/端点，提供按类型分组的各账户分量。

## Impact

- **代码**：`frontend-vue`（`main.ts`、`vite.config.ts`、`DashboardPage.vue`、可能新增小组件）、可选 **Spring** `DashboardController` / `DashboardService`。
- **依赖与构建**：Vite `build.rollupOptions.output.manualChunks` 或等价策略；Naive 按需与入口注册方式调整。
- **部署**：静态资源 hash 与缓存策略不变；若扩展 API，需同步部署后端。
