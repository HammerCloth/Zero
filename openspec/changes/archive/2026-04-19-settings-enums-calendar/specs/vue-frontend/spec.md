## ADDED Requirements

### Requirement: 设置与枚举数据

前端 SHALL 提供对设置 API 的访问模块（例如 `src/api/settings.ts`），并 SHALL 使用 Pinia store（例如 `settingsStore`）缓存账户类型、归属、大事记分类等选项，供账户页、快照表单、大事记统计等复用。

#### Scenario: 登录后可用

- **WHEN** 用户已登录并进入需枚举的页面
- **THEN** 页面 SHALL 展示来自后端的标签，且 SHALL NOT 仅依赖前端硬编码英文枚举作为唯一数据源

#### Scenario: 设置更新后刷新

- **WHEN** 用户在设置页保存新选项
- **THEN** 其他页面的下拉与标签 SHALL 反映更新（通过刷新 store 或重新拉取）

### Requirement: 日期选择组件

涉及 `YYYY-MM-DD` 的业务日期（至少包含快照日期）SHALL 使用 Naive UI 日期选择类组件绑定，且 SHALL 与后端字符串格式一致。

#### Scenario: 格式一致

- **WHEN** 用户选择日期并提交
- **THEN** 请求体中的日期字符串 SHALL 为 ISO 日期格式 `YYYY-MM-DD`
