## Context

Spring + Vue 版本已上线：账户 `type`/`owner`、大事记 `category` 在后端 `AccountValidation` / `EventValidation` 与前端常量中硬编码；快照日期在 `SnapshotFormPage` 为纯文本输入。用户希望从**日历按日进入**编辑或新建，并在**设置**中维护可选项。数据为单用户 SQLite，需兼容已有行内存储的英文 key。

## Goals / Non-Goals

**Goals:**

- 提供「按日期」解析路由或查询：某日有快照 → 进详情或编辑；无快照 → 新建并预填日期。
- 将账户类型、归属、大事记分类改为**每用户可配置**列表（每项含稳定 `key` 与展示 `label`，可选 `sortOrder` / `enabled`）。
- 后端校验与前端下拉均以配置为准；首次部署或新用户写入**与当前硬编码一致的默认种子**。
- 快照/大事记表单使用日期选择器；大事记在 UI 上明确**收入 vs 支出**（与金额正负规则一致）。
- 统计与列表展示分类、类型、归属时使用**中文标签**（来自配置）。

**Non-Goals:**

- 多租户隔离（仍按现有 `user_id` 作用域即可）。
- 全站视觉重设计、换主题系统。
- 修改净资产计算公式或快照唯一性规则（仍为一用户一日一条快照）。

## Decisions

**1. 配置存储模型**

- **选用**：单独表 `user_option_definitions`（或按维度分表），字段示例：`user_id`、`dimension`（`account_type` | `account_owner` | `event_category`）、`key`（英文 slug，唯一于 user+dimension）、`label`（展示文案）、`sort_order`、`enabled`、`created_at`。
- **理由**：便于 SQL 迁移、排序与启用开关；比纯 JSON 列更易做唯一约束与增量同步。
- **备选**：单表 JSON blob — 实现快但校验与迁移弱，否决。

**2. Key 与历史数据**

- **选用**：继续用字符串 `key` 存业务表；配置项删除时**禁止**若仍被引用，或提供「软删除 + 历史仍显示旧标签」策略（设计推荐：**禁用**而非删除，或删除前校验引用）。
- **理由**：避免级联改写历史快照。

**3. 默认种子**

- **选用**：Flyway 迁移或应用启动时「若该用户某 dimension 无记录则插入」当前 `AccountValidation` / `EventValidation` 等价集合。
- **理由**：满足提案「默认与现网枚举等价」，降低 BREAKING。

**4. 按日导航 API**

- **选用**：`GET /api/v1/snapshots/for-date?date=YYYY-MM-DD` 返回 `{ snapshot: null | { id, ... } }`，或等价路径名；前端日历点击后调用再 `router.push`。
- **理由**：避免前端拉全量列表再过滤；与现有 REST 风格一致。

**5. 大事记收支**

- **选用**：保持金额存储为**有符号数**；正数表示收入、负数表示支出（与现有「负数支出」统计逻辑对齐时，统计支出仍取支出侧绝对值或分维度规则在 spec 中写死）。表单上增加**收支类型**切换或明确文案，提交时转换为符号。
- **备选**：双字段 `income`/`expense` — 改动面大，暂不取。

**6. 前端枚举加载**

- **选用**：登录后或进入相关页前拉取 `GET /api/v1/settings/options`（或分维度多个 GET），Pinia `settingsStore` 缓存；设置页更新后 invalidate 缓存。
- **理由**：减少每表单重复请求，与现有 Pinia 一致。

## Risks / Trade-offs

- **[Risk]** 用户清空某维度所有选项 → 无法创建账户/大事记。  
  **Mitigation**：至少保留一条启用项；服务端拒绝删光。

- **[Risk]** 自定义 key 与旧数据不一致导致「未知」显示。  
  **Mitigation**：列表显示 `label(unknown_key)` 或仅 key；设置中提供「恢复默认」。

- **[Risk]** 日历组件包体积。  
  **Mitigation**：使用 Naive 内置日历/日期面板或轻量 `n-calendar` + 已有 `n-date-picker`，避免引入过重第三方。

## Migration Plan

1. Flyway：新增选项表 + 种子数据（或应用内 seed）。
2. 部署后端：新 API 可用后，旧前端仍用固定枚举会与新校验**冲突** — 需**同一版本**前后端一起发，或后端短期内同时接受旧枚举与配置（不推荐），故采用**单版本联调发布**。
3. 回滚：回退迁移脚本或保留表不读；代码回滚到硬编码校验。

## Open Questions

- 日历主入口放在 `/snapshots` 同页 Tab，还是独立 `/snapshots/calendar`（提案倾向可独立路由，具体 UX 实现时定）。
- `GET for-date` 是否合并进现有 `list` 的 query 参数以减少端点数（可二选一，任务阶段定）。
