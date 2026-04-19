## ADDED Requirements

### Requirement: 快照日历视图

系统 SHALL 提供快照日历视图路由（例如 `/snapshots/calendar`），以月历形式展示哪些日期已有快照；用户 SHALL 能够点击某日进入该日流程。

#### Scenario: 有快照的日期

- **WHEN** 用户点击已有快照的日期
- **THEN** 系统 SHALL 导航至该快照详情或编辑页（与产品选择一致，但 SHALL 不丢失目标日期）

#### Scenario: 无快照的日期

- **WHEN** 用户点击无快照的日期
- **THEN** 系统 SHALL 导航至新建快照页并 SHALL 预填该日日期

#### Scenario: 与 API 一致

- **WHEN** 日历加载或切换月份
- **THEN** 前端 SHALL 使用按日查询或批量接口获取各日是否有快照，且 SHALL 与后端数据一致

## MODIFIED Requirements

### Requirement: 快照详情页

系统 SHALL 提供快照详情页 /snapshots/{id}，显示快照明细和大事记；大事记表格 SHALL 使用设置中的分类 `label` 展示 category，且 SHALL 以用户可理解的方式展示金额方向（收入/支出或等价符号说明）。

#### Scenario: 明细展示

- **WHEN** 访问 /snapshots/123
- **THEN** 显示该快照日期、各账户余额、大事记列表

#### Scenario: 编辑按钮

- **WHEN** 点击「编辑」
- **THEN** 跳转到 /snapshots/123/edit

#### Scenario: 删除按钮

- **WHEN** 点击「删除」并确认
- **THEN** 删除快照，返回列表页

#### Scenario: 大事记可读性

- **WHEN** 查看大事记列表
- **THEN** 每条 SHALL 能区分收入与支出含义，且分类 SHALL 显示为中文标签（来自设置）

### Requirement: 快照列表页

系统 SHALL 提供快照列表页 /snapshots，显示所有快照，按日期倒序；页面 SHALL 提供进入日历视图的入口（链接或 Tab）。

#### Scenario: 列表显示

- **WHEN** 访问 /snapshots
- **THEN** 显示快照卡片列表，每张卡片包含日期、净资产

#### Scenario: 新建按钮

- **WHEN** 点击「新建快照」
- **THEN** 跳转到 /snapshots/new

#### Scenario: 点击卡片

- **WHEN** 点击某个快照卡片
- **THEN** 跳转到 /snapshots/{id}

#### Scenario: 进入日历

- **WHEN** 用户点击「日历」或等价入口
- **THEN** 跳转到快照日历视图路由

### Requirement: 新建快照页

系统 SHALL 提供新建快照页 /snapshots/new，支持选择日期、各账户余额、大事记；日期 SHALL 通过日期选择器（例如 Naive UI DatePicker）选择，SHALL NOT 仅依赖无约束的纯文本输入。

#### Scenario: 表单结构

- **WHEN** 访问 /snapshots/new
- **THEN** 显示日期选择器、账户余额表单、大事记添加区域

#### Scenario: 从日历预填日期

- **WHEN** 自日历视图跳转且 URL 携带 `date=YYYY-MM-DD` 查询参数或等价状态
- **THEN** 日期选择器 SHALL 预填该日

#### Scenario: 保存成功

- **WHEN** 填写完整数据，点击「保存」
- **THEN** 创建快照，跳转到详情页

#### Scenario: 余额校验

- **WHEN** 某账户余额未填写，点击「保存」
- **THEN** 显示「存在未填写余额的账户」提示

### Requirement: 大事记表单

新建/编辑快照时 SHALL 支持添加多条大事记，每条包含分类下拉、描述输入、金额输入；分类选项 SHALL 来自设置中的 `event_category`；金额输入区域 SHALL 明确区分收入与支出（例如类型切换或文案），并与有符号金额规则一致。

#### Scenario: 添加大事记

- **WHEN** 点击「添加大事记」
- **THEN** 显示新的大事记表单行

#### Scenario: 删除大事记

- **WHEN** 点击大事记行的删除按钮
- **THEN** 移除该行

#### Scenario: 收支语义可见

- **WHEN** 用户查看或编辑大事记金额
- **THEN** 界面 SHALL 能区分当前为收入还是支出，且 SHALL 与提交后的正负号一致
