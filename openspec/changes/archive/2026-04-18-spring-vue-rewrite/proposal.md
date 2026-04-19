## Why

当前 Go + React 技术栈存在多个问题：
- 后端 Go 代码维护者不熟悉，难以排查问题和扩展功能
- 部署过程中频繁出现 bug（迁移文件丢失、CORS 配置、路由匹配等）
- 前端 React 代码维护者不熟悉，UI 设计不理想

维护者更熟悉 Spring + MyBatis + Vue 技术栈，迁移到熟悉的技术栈可以：
- 降低维护成本，能自主判断 AI 生成代码的正确性
- 从根本上解决现有架构的部署问题
- 借助 Naive UI 获得更好的视觉效果

## What Changes

**后端重写（Go → Spring Boot）**
- 使用 Spring Boot 3 + Java 21 重写全部后端 API
- 使用 MyBatis 替代 Go 的手写 SQL
- 使用 Flyway 管理数据库迁移，彻底解决"迁移文件找不到"问题
- 使用 Spring Security + JJWT 实现 JWT 认证
- 修复 Cookie 安全性问题（添加 Secure + SameSite）

**前端重写（React → Vue）**
- 使用 Vue 3 + Vite + TypeScript 重写全部前端
- 使用 Naive UI 组件库替代 Tailwind 手写样式
- 使用 Pinia 进行状态管理
- 重新设计 UI，提升视觉效果

**数据库**
- 继续使用 SQLite（2C2G 服务器内存友好）
- 数据重置，不做迁移（现有数据量少）

**部署**
- 更新 Dockerfile（Java 镜像）
- 更新 docker-compose.yml
- Caddy 配置基本不变

**BREAKING**
- API 路径可能调整（前后端同步重写，不需要保持兼容）
- 前端完全重写，无法复用现有代码

## Capabilities

### New Capabilities
- `spring-backend`: Spring Boot 后端骨架（项目结构、配置、Flyway、安全配置）
- `auth-api`: 认证模块 API（登录、注册、JWT、刷新、登出）
- `user-api`: 用户管理 API（列表、创建、重置密码）
- `account-api`: 账户管理 API（增删改查、排序）
- `snapshot-api`: 快照管理 API（增删改查、明细）
- `dashboard-api`: 仪表盘 API（汇总、趋势、构成、月度增长）
- `event-api`: 大事记 API（统计）
- `export-api`: 导出 API（CSV）
- `vue-frontend`: Vue 3 前端骨架（项目结构、路由、Pinia、Axios）
- `auth-pages`: 认证相关页面（登录、初始化、修改密码）
- `dashboard-page`: 仪表盘页面（图表、卡片）
- `snapshot-pages`: 快照相关页面（列表、详情、表单）
- `account-page`: 账户管理页面
- `event-page`: 大事记统计页面
- `user-page`: 用户管理页面（管理员）

### Modified Capabilities
（无，这是完全重写，不是修改现有能力）

## Impact

**代码**
- `backend/` 目录：完全删除，替换为 Spring Boot 项目
- `frontend/` 目录：完全删除，替换为 Vue 3 项目

**API**
- 所有 API 路径重新设计
- 保持 RESTful 风格，但不强求与现有路径兼容

**依赖**
- 后端：Go → Java 21 + Spring Boot 3 + MyBatis + SQLite JDBC
- 前端：React → Vue 3 + Naive UI + Pinia + Axios

**部署**
- Dockerfile 需要更换为 Java 镜像
- docker-compose 需要调整 backend 服务配置
- Caddy 配置基本不变（反代 /api 到后端，其余走前端）

**数据**
- SQLite 数据库结构可能微调
- 现有数据会被重置
