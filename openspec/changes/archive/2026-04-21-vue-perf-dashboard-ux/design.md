## Context

- 仅维护 **Vue** 前端（`frontend-vue`）；路由已按页 `import()` 懒加载，但 **入口仍 `app.use(naive)` 全量注册**，与 `openspec/specs/vue-frontend` 中「按需导入」不一致，易把大量 Naive 打进首包。
- **ECharts** 在 `DashboardPage.vue`、`EventStatsPage.vue` 中按需 `use([...])`，构建产物仍出现 **数百 KB 级 vendor chunk**（如 `dist-*.js`），首访下载时间长。
- `DashboardPage.vue` 中资产构成两段使用 **`n-grid :cols="2"` 固定两列**，与「移动端单列、饼图完整可见」的意图冲突。
- 现有 `GET /api/v1/dashboard/composition` 仅返回 `byType` / `byOwner` 扁平聚合；「类型内账户占比」需要 **二维结构** 或前端可稳定还原的数据。

## Goals / Non-Goals

**Goals:**

- 降低 **首屏关键路径** 上的传输字节与解析成本（gzip 已开的前提下仍以减体积为主）。
- 仪表盘在 **小视口** 下图表布局可读、不构成图被压扁。
- 用户可理解 **每个资产类型内部** 各账户的相对占比（与最新快照一致）。

**Non-Goals:**

- 不改造 React 前端（已确认不使用）。
- 不在本变更内做通用 CDN 架构替换、HTTP/3 等纯运维项（可在部署层并行）。
- 不要求与 Immersive Translate 等浏览器扩展的请求行为对标。

## Decisions

1. **Naive UI 注册方式**  
   - **选择**：移除入口 `app.use(naive)` 全量注册，依赖已有 **`unplugin-vue-components` + `NaiveUiResolver`** 实现按需；按需补充全局样式入口（若官方要求 `naive-ui` 样式在 `main.ts` 单条 CSS import，保留该条，不引入全组件注册）。  
   - **备选**：改用手动 per-component import；维护成本高，不作为首选。

2. **Vite 拆包**  
   - **选择**：在 `vite.config.ts` 使用 `build.rollupOptions.output.manualChunks`，将 **`echarts` + `vue-echarts`** 打入独立异步 chunk（名称稳定如 `echarts-vendor`），与 `vue`、`naive-ui`（若仍较大则再拆）分离，便于缓存与并行下载。  
   - **权衡**：chunk 数略增；单次连接 HTTP/2 下仍可接受。避免过碎（>20 个小 chunk）以免 RTT 放大。

3. **ECharts 加载范围**  
   - **选择**：保持 ECharts 仅在含图表的页面 chunk 中；若 `EventStatsPage` 与 `DashboardPage` 共享同一 echarts vendor chunk，由 Rollup 自动合并重复依赖即可。  
   - **备选**：动态 `import('vue-echarts')` 封装异步图表壳组件——收益有限时可在后续迭代再做。

4. **移动端构成图布局**  
   - **选择**：资产构成两卡片使用 **响应式栅格**：小屏 `span=24`（单列），中大屏两列（与顶部统计卡片 `responsive="screen"` 模式对齐）。饼图 `option` 增加 **`legend`（scroll）或 `label` 布局**，`radius` 略减小以保证标签不溢出卡片。  
   - **备选**：小屏改为列表 + 数值；信息密度变化大，本阶段不做。

5. **「类型内账户占比」数据来源**  
   - **选择（推荐）**：后端 **`GET /api/v1/dashboard/composition`** 扩展响应，增加例如 `byTypeAccounts: Record<string, Record<string, number>>`（外层 key 为 `account_type`，内层为 `account_id` → 金额，负债类型符号与现有 `composition` 一致），或新增并列端点 `GET /api/v1/dashboard/composition-accounts` 由前端二选一实现。  
   - **理由**：口径与 `DashboardService` 内快照聚合一致，避免前端再拉多接口拼快照。  
   - **备选**：仅前端用 `account-trends` 最后一点近似——与「构成」口径可能不一致，**不推荐**作为主方案。

## Risks / Trade-offs

- **[Risk] 按需 Naive 后遗漏某全局组件（如 message、dialog）** → 按报错补 `n-` 组件或显式注册缺失项；用 E2E/手测登录与仪表盘路径覆盖。  
- **[Risk] manualChunks 配置过激进导致循环依赖或运行时加载顺序问题** → 先保守只拆 `echarts`/`zrender`，构建与预览验证。  
- **[Risk] API 扩展破坏旧客户端** → 仅 **新增字段**，旧字段不变；版本兼容。  
- **[Trade-off] 账户很多时图例过长** → ECharts `legend: { type: 'scroll' }` + 限制展示条数或合并长尾为「其他」（可在 tasks 中列为可选优化）。

## Migration Plan

1. 先合并并部署 **后端 API 扩展**（若采用扩展字段），再发前端；或同一版本原子发布。  
2. 前端发布后：用户硬刷新一次即可拿到新 chunk；CDN 缓存依赖文件名 hash，无额外迁移。  
3. **回滚**：还原 `vite` 配置与 `main.ts`；API 新字段可保留（忽略即可）或 feature flag（若引入）。

## Open Questions

- 「类型内账户占比」是否 **包含负债类型** 下钻到账户层（与饼图扇区是否一致）——默认与 `byType` 同口径，负债为负值时在图上用单独说明或仅展示绝对值堆叠，需产品拍板。  
- 账户数极多时是否要做 **Top N + 其他**（默认不做，留作性能优化项）。
