## ADDED Requirements

### Requirement: 账户管理页
系统 SHALL 提供账户管理页 /accounts，显示所有账户（含已停用）。

#### Scenario: 列表显示
- **WHEN** 访问 /accounts
- **THEN** 显示账户表格，包含名称、类型、归属、状态

#### Scenario: 停用账户样式
- **WHEN** 账户 isActive=false
- **THEN** 显示为灰色/删除线样式

### Requirement: 新建账户
系统 SHALL 支持新建账户，填写名称、类型、归属。

#### Scenario: 打开对话框
- **WHEN** 点击「新建账户」
- **THEN** 弹出对话框，包含名称输入、类型下拉、归属下拉

#### Scenario: 创建成功
- **WHEN** 填写信息，点击「确定」
- **THEN** 创建账户，刷新列表

### Requirement: 编辑账户
系统 SHALL 支持编辑账户信息。

#### Scenario: 打开编辑
- **WHEN** 点击账户行的「编辑」按钮
- **THEN** 弹出对话框，预填现有信息

#### Scenario: 更新成功
- **WHEN** 修改信息，点击「确定」
- **THEN** 更新账户，刷新列表

### Requirement: 停用账户
系统 SHALL 支持停用账户（软删除）。

#### Scenario: 停用操作
- **WHEN** 点击账户行的「停用」按钮并确认
- **THEN** 账户变为停用状态

### Requirement: 账户排序
系统 SHALL 支持拖拽排序账户。

#### Scenario: 拖拽排序
- **WHEN** 拖动账户行到新位置
- **THEN** 保存新的排序顺序

### Requirement: 账户类型显示
账户类型 SHALL 显示为中文标签：现金、固收、基金、养老、公积金、负债。

#### Scenario: 类型标签
- **WHEN** 账户类型为 fund
- **THEN** 显示为「基金」

### Requirement: 归属显示
账户归属 SHALL 显示为中文标签：成员 A、成员 B、共同。

#### Scenario: 归属标签
- **WHEN** 账户归属为 shared
- **THEN** 显示为「共同」
