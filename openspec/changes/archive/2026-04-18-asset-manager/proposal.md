## Why

家庭财务管理混乱，不清楚净资产有多少、是否在增长、大额支出花在哪里。需要一个轻量级系统，在 2C2G 服务器上运行，帮助追踪资产变化趋势，每月花 5 分钟录入即可掌握财务全貌。

## What Changes

- 新建完整的资产管家系统（从零开始）
- 提供安全的用户认证（首次启动交互设置、密码哈希、JWT）
- 支持账户管理（11 个账户：现金/固收/基金/养老/公积金/信用卡）
- 支持月度快照录入（自动复制上次数据，只改变化的）
- 支持大事记功能（记录大额支出：房租/旅行/医疗等）
- 提供资产总览图表（净资产趋势、资产构成、月度增长等 6 种图表）
- 响应式设计，支持移动端访问

## Capabilities

### New Capabilities

- `user-auth`: 用户认证系统，包括首次启动设置、登录登出、密码管理、JWT Token
- `account-management`: 账户管理，支持多种账户类型的增删改查和排序
- `snapshot`: 快照录入系统，支持创建、编辑、删除快照，自动复制上次数据
- `events`: 大事记（大额支出）记录，按分类统计
- `dashboard`: 资产总览仪表盘，包含关键指标和多种图表
- `api`: RESTful API 后端，Go + Gin + SQLite 实现

### Modified Capabilities

（无，这是全新项目）

## Impact

- **代码**: 新建 backend/ (Go) 和 frontend/ (React) 目录
- **数据库**: 新建 SQLite 数据库，包含 users、accounts、snapshots、snapshot_items、events 表
- **部署**: 需要 Docker Compose + Caddy 配置
- **依赖**: Go 1.21+, Node.js 20+, 需要域名用于 HTTPS
