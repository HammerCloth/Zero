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
仪表盘 SHALL 显示 4 个指标卡片：当前净资产、本期变化、年初至今、年化收益率。

#### Scenario: 卡片显示
- **WHEN** 页面加载完成
- **THEN** 显示 4 个卡片，数值格式化为货币/百分比

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

