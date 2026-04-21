## MODIFIED Requirements

### Requirement: Naive UI 组件库

前端 SHALL 使用 Naive UI 作为 UI 组件库；组件 SHALL 通过按需机制引入（例如 `unplugin-vue-components` 与 `NaiveUiResolver` 自动解析模板中的 `n-*` 组件，或等价官方按需方案）。应用入口 SHALL NOT 调用 `app.use(naive)` 等方式注册**完整** Naive 包以致生产构建无法对未用组件树摇。

#### Scenario: 组件可用

- **WHEN** 在 Vue 组件中使用 `<n-button>`
- **THEN** 正确渲染 Naive UI 按钮

#### Scenario: 入口不全量注册

- **WHEN** 审查 `src/main.ts`（或应用入口等价文件）
- **THEN** 不存在对完整 `naive` 包的 `app.use(naive)` 全量注册

## ADDED Requirements

### Requirement: 生产构建与 vendor 分割

前端 SHALL 通过 Vite 生产配置将 **ECharts**（及 `zrender` 等其硬依赖）与 **`vue-echarts`** 拆入独立异步 chunk（例如 `manualChunks` 规则），使含大量图表依赖的代码不与其他业务入口过度耦合到单一大文件；构建产物 SHALL 保持可成功 `npm run build`。

#### Scenario: 构建成功且存在 echarts 相关 chunk

- **WHEN** 执行 `npm run build`
- **THEN** 构建成功完成
- **THEN** 产物中存在可识别的独立 chunk（文件名以构建哈希为准），其中包含来自 `echarts` 或 `vue-echarts` 的模块（以 rollup 输出或 bundle 分析为准）
