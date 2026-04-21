# dashboard-page Specification

## Purpose

Vue 仪表盘路由 `/dashboard`：展示净资产、趋势、构成、月度变化、堆叠与按账户趋势等图表；数据来自 `/api/v1/dashboard/*` 与 `settingsStore` 标签映射；支持导出 CSV。
## Requirements
### Requirement: 仪表盘页面
系统 SHALL 提供仪表盘页面 /dashboard 作为登录后的默认首页。

#### Scenario: 默认跳转
- **WHEN** 登录成功或访问根路径
- **THEN** 跳转到 /dashboard

### Requirement: 指标卡片
仪表盘 SHALL 在「资产概览」区域展示当前净资产、本期变化、年初至今变化、年化收益率；四项 SHALL 在同一卡片内以单行（或多列网格）紧凑展示，数值格式化为货币/百分比。

#### Scenario: 卡片显示
- **WHEN** 页面加载完成
- **THEN** 显示上述四项指标，布局紧凑可读

#### Scenario: 变化颜色
- **WHEN** 本期变化为正
- **THEN** 数值显示为绿色

#### Scenario: 变化为负
- **WHEN** 本期变化为负
- **THEN** 数值显示为红色

### Requirement: 净资产趋势图
仪表盘 SHALL 显示净资产趋势折线图，支持时间范围切换（3个月/6个月/1年/全部）。

#### Scenario: 范围切换
- **WHEN** 点击「1年」按钮
- **THEN** 图表只显示最近一年的数据

#### Scenario: 导出按钮
- **WHEN** 点击「导出数据(CSV)」
- **THEN** 下载 snapshots.csv 文件

### Requirement: 资产构成图
仪表盘 SHALL 显示两个饼图：按类型分布、按归属分布。

#### Scenario: 类型分布
- **WHEN** 查看「资产构成（类型）」
- **THEN** 显示饼图，包含各类型占比

#### Scenario: 归属分布
- **WHEN** 查看「资产构成（归属）」
- **THEN** 显示饼图，包含 A/B/共同 占比

### Requirement: 月度增长图
仪表盘 SHALL 显示当年每月净资产变化的柱状图。

#### Scenario: 月度数据
- **WHEN** 查看「月度净资产变化」
- **THEN** 显示 1-12 月的柱状图

### Requirement: 资产堆叠图
仪表盘 SHALL 显示按资产类型堆叠的面积图，展示各类资产历史变化。

#### Scenario: 堆叠展示
- **WHEN** 查看「资产堆叠（按类型）」
- **THEN** 显示各类型资产的堆叠面积图

### Requirement: 账户明细趋势
仪表盘 SHALL 显示各账户余额趋势图，支持按类型筛选。

#### Scenario: 筛选类型
- **WHEN** 选择「基金」筛选
- **THEN** 只显示基金类型账户的趋势

### Requirement: 加载状态
仪表盘 SHALL 在数据加载时显示加载指示器。

#### Scenario: 加载中
- **WHEN** API 请求进行中
- **THEN** 显示加载动画

#### Scenario: 加载失败
- **WHEN** API 请求失败
- **THEN** 显示错误提示

### Requirement: 资产构成移动端布局
在 `DashboardPage.vue` 中，「资产构成（类型）」与「资产构成（归属）」所在区域 SHALL 在视口宽度小于 768px 时以**单列**展示；每个卡片内饼图 SHALL 具备可读图例或标签（例如 ECharts `legend` 滚动或标签换行），不得因固定双列栅格导致图例被裁切无法阅读。

#### Scenario: 窄屏单列
- **WHEN** 在宽度小于 768px 的视口打开 `/dashboard`
- **THEN** 两个资产构成卡片纵向排列，各占接近全宽

#### Scenario: 图例可读
- **WHEN** 构成数据非空且账户/类型项较多
- **THEN** 用户仍可通过图例或标签识别各扇区含义（支持滚动或折叠策略）

### Requirement: 类型内账户占比图
仪表盘 SHALL 展示「各资产类型下各账户占比」：用户 SHALL 能选择资产类型（例如下拉），并以饼图展示该类型下各账户金额占比；数据来自 `GET /api/v1/dashboard/composition` 的 `byTypeAccounts`（或等价字段）；类型标签与账户名称与 `settingsStore` 及账户列表一致。

#### Scenario: 有数据时展示
- **WHEN** API 返回 `byTypeAccounts` 且某类型下存在可展示余额
- **THEN** 用户可选择类型并看到对应饼图，数值与接口一致

#### Scenario: 无分账户数据时
- **WHEN** 接口无 `byTypeAccounts` 或为空（旧后端或回滚）
- **THEN** 页面不崩溃，显示空状态或提示
