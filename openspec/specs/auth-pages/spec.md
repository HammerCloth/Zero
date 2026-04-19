# auth-pages Specification

## Purpose
TBD - created by archiving change spring-vue-rewrite. Update Purpose after archive.
## Requirements
### Requirement: 初始化页面
系统 SHALL 提供初始化页面 /setup，在系统未初始化时显示，允许创建第一个管理员。

#### Scenario: 首次访问
- **WHEN** 系统未初始化，访问任意页面
- **THEN** 重定向到 /setup

#### Scenario: 初始化成功
- **WHEN** 填写用户名和密码，点击「创建并进入」
- **THEN** 创建管理员，跳转到仪表盘

#### Scenario: 系统已初始化
- **WHEN** 系统已初始化，访问 /setup
- **THEN** 重定向到登录页

### Requirement: 登录页面
系统 SHALL 提供登录页面 /login，包含用户名、密码输入框和「记住我」选项。

#### Scenario: 登录成功
- **WHEN** 输入正确的用户名密码，点击「登录」
- **THEN** 跳转到仪表盘

#### Scenario: 登录失败
- **WHEN** 输入错误的密码
- **THEN** 显示「用户名或密码错误」提示

#### Scenario: 已登录访问
- **WHEN** 已登录用户访问 /login
- **THEN** 重定向到仪表盘

### Requirement: 修改密码页面
系统 SHALL 提供修改密码页面 /change-password，需要登录才能访问。

#### Scenario: 强制修改密码
- **WHEN** 用户 mustChangePassword=true 且访问其他页面
- **THEN** 强制跳转到 /change-password

#### Scenario: 修改成功
- **WHEN** 输入当前密码和新密码，点击「保存」
- **THEN** 密码更新成功，跳转到仪表盘

#### Scenario: 当前密码错误
- **WHEN** 输入错误的当前密码
- **THEN** 显示「当前密码错误」提示

### Requirement: 页面样式
认证相关页面 SHALL 居中显示卡片式表单，简洁明了。

#### Scenario: 视觉风格
- **WHEN** 查看登录/初始化页面
- **THEN** 显示居中的白色卡片，包含标题、表单和按钮

