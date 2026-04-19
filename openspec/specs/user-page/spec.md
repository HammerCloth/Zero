# user-page Specification

## Purpose
TBD - created by archiving change spring-vue-rewrite. Update Purpose after archive.
## Requirements
### Requirement: 用户管理页
系统 SHALL 提供用户管理页 /users，仅管理员可访问。

#### Scenario: 权限控制
- **WHEN** 非管理员访问 /users
- **THEN** 重定向到仪表盘

#### Scenario: 管理员访问
- **WHEN** 管理员访问 /users
- **THEN** 显示用户列表

### Requirement: 用户列表
页面 SHALL 显示所有用户的表格，包含用户名、角色、创建时间。

#### Scenario: 列表内容
- **WHEN** 查看用户列表
- **THEN** 显示所有用户，管理员标记为「管理员」

### Requirement: 创建用户
页面 SHALL 支持创建新用户。

#### Scenario: 打开对话框
- **WHEN** 点击「新建用户」
- **THEN** 弹出对话框，包含用户名、密码、是否管理员

#### Scenario: 创建成功
- **WHEN** 填写信息，点击「确定」
- **THEN** 创建用户，刷新列表

#### Scenario: 用户名重复
- **WHEN** 用户名已存在
- **THEN** 显示「用户名已存在」提示

### Requirement: 重置密码
页面 SHALL 支持重置用户密码。

#### Scenario: 重置操作
- **WHEN** 点击用户行的「重置密码」
- **THEN** 弹出对话框，输入新密码

#### Scenario: 重置成功
- **WHEN** 输入新密码，点击「确定」
- **THEN** 密码重置成功，用户下次登录需修改密码

### Requirement: 导航入口
用户管理入口 SHALL 仅对管理员显示在侧边栏/底部导航。

#### Scenario: 管理员导航
- **WHEN** 当前用户是管理员
- **THEN** 导航栏显示「用户管理」入口

#### Scenario: 普通用户导航
- **WHEN** 当前用户不是管理员
- **THEN** 导航栏不显示「用户管理」

