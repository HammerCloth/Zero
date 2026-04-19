## ADDED Requirements

### Requirement: 用户选项维度

系统 SHALL 将可配置枚举划分为至少三个维度：`account_type`（账户类型）、`account_owner`（账户归属）、`event_category`（大事记分类）。每个维度下包含若干选项，每项 SHALL 具有稳定 `key` 与用户可见 `label`，并 SHALL 支持 `sortOrder` 与启用状态。

#### Scenario: 维度可查询

- **WHEN** 调用选项列表 API
- **THEN** 返回按维度分组的选项，且仅包含当前登录用户有权访问的数据

### Requirement: 读取选项

系统 SHALL 提供 GET 端点（例如 `GET /api/settings/options` 或分维度子路径）返回当前用户的全部选项或指定维度的选项，供前端渲染下拉框与标签映射。

#### Scenario: 成功返回

- **WHEN** GET 选项接口且用户已登录
- **THEN** 返回 200 和结构化 JSON，包含各维度的 `key`、`label`、`sortOrder`、`enabled`

### Requirement: 更新选项

系统 SHALL 提供 PUT/PATCH 端点允许用户替换某维度下的选项列表（增删改顺序与标签），并 SHALL 在校验失败时返回 400。

#### Scenario: 非法 key

- **WHEN** 提交空 key、重复 key 或不符合约定格式（如含空格）
- **THEN** 返回 400 与明确错误信息

#### Scenario: 删除仍被引用的 key

- **WHEN** 用户尝试删除仍被账户或大事记引用的 key
- **THEN** 返回 409 或业务错误，且 SHALL NOT 静默破坏历史数据完整性

### Requirement: 默认种子

系统 SHALL 在新用户首次需要选项数据或某维度无记录时，插入与当前硬编码枚举等价的默认选项集，以保证现有业务规则可延续。

#### Scenario: 新用户首次访问

- **WHEN** 用户首次拉取选项且数据库中无该维度记录
- **THEN** 系统自动种子化默认 `key`/`label` 集合后再返回
