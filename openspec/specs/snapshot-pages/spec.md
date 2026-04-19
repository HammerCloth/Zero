# snapshot-pages Specification

## Purpose
TBD - created by archiving change spring-vue-rewrite. Update Purpose after archive.
## Requirements
### Requirement: 快照列表页
系统 SHALL 提供快照列表页 /snapshots，显示所有快照，按日期倒序。

#### Scenario: 列表显示
- **WHEN** 访问 /snapshots
- **THEN** 显示快照卡片列表，每张卡片包含日期、净资产

#### Scenario: 新建按钮
- **WHEN** 点击「新建快照」
- **THEN** 跳转到 /snapshots/new

#### Scenario: 点击卡片
- **WHEN** 点击某个快照卡片
- **THEN** 跳转到 /snapshots/{id}

### Requirement: 快照详情页
系统 SHALL 提供快照详情页 /snapshots/{id}，显示快照明细和大事记。

#### Scenario: 明细展示
- **WHEN** 访问 /snapshots/123
- **THEN** 显示该快照日期、各账户余额、大事记列表

#### Scenario: 编辑按钮
- **WHEN** 点击「编辑」
- **THEN** 跳转到 /snapshots/123/edit

#### Scenario: 删除按钮
- **WHEN** 点击「删除」并确认
- **THEN** 删除快照，返回列表页

### Requirement: 新建快照页
系统 SHALL 提供新建快照页 /snapshots/new，支持填写日期、各账户余额、大事记。

#### Scenario: 表单结构
- **WHEN** 访问 /snapshots/new
- **THEN** 显示日期选择器、账户余额表单、大事记添加区域

#### Scenario: 保存成功
- **WHEN** 填写完整数据，点击「保存」
- **THEN** 创建快照，跳转到详情页

#### Scenario: 余额校验
- **WHEN** 某账户余额未填写，点击「保存」
- **THEN** 显示「存在未填写余额的账户」提示

### Requirement: 编辑快照页
系统 SHALL 提供编辑快照页 /snapshots/{id}/edit，与新建页类似但预填现有数据。

#### Scenario: 数据预填
- **WHEN** 访问 /snapshots/123/edit
- **THEN** 表单预填现有日期、余额、大事记

#### Scenario: 更新成功
- **WHEN** 修改数据，点击「保存」
- **THEN** 更新快照，跳转到详情页

### Requirement: 大事记表单
新建/编辑快照时 SHALL 支持添加多条大事记，每条包含分类下拉、描述输入、金额输入。

#### Scenario: 添加大事记
- **WHEN** 点击「添加大事记」
- **THEN** 显示新的大事记表单行

#### Scenario: 删除大事记
- **WHEN** 点击大事记行的删除按钮
- **THEN** 移除该行

### Requirement: 账户余额表单
新建/编辑快照时 SHALL 显示所有活跃账户的余额输入框，按 sortOrder 排序。

#### Scenario: 账户列表
- **WHEN** 打开新建快照页
- **THEN** 显示所有活跃账户的余额输入框

#### Scenario: 负债账户
- **WHEN** 账户类型为 credit
- **THEN** 余额输入提示用户输入正数（系统自动取负）

