## ADDED Requirements

### Requirement: 资产构成移动端布局

在 `DashboardPage.vue` 中，「资产构成（类型）」与「资产构成（归属）」所在区域 SHALL 在视口宽度小于 768px 时以**单列**展示；每个卡片内饼图 SHALL 具备可读图例或标签（例如 ECharts `legend` 滚动或标签换行），不得因固定双列栅格导致图例被裁切无法阅读。

#### Scenario: 窄屏单列

- **WHEN** 在宽度小于 768px 的视口打开 `/dashboard`
- **THEN** 两个资产构成卡片纵向排列，各占接近全宽

#### Scenario: 图例可读

- **WHEN** 构成数据非空且账户/类型项较多
- **THEN** 用户仍可通过图例或标签识别各扇区含义（支持滚动或折叠策略）

### Requirement: 类型内账户占比图

仪表盘 SHALL 在合适位置（建议紧邻现有构成图或单独卡片）展示「各资产类型下各账户占比」图，数据来自扩展后的 `GET /api/v1/dashboard/composition`（或本变更设计的等价只读端点）；展示维度与 `settingsStore` 中 `account_type` / 账户名称一致。

#### Scenario: 有数据时展示

- **WHEN** API 返回包含按类型分账户的分量字段且存在多账户
- **THEN** 页面展示对应图表且数值与接口一致

#### Scenario: 无分账户数据时

- **WHEN** 接口仅返回旧字段（实现回滚或旧后端）
- **THEN** 页面不崩溃；新图可为空或隐藏（以实现约定为准，须在 tasks 中明确）
