## 1. 项目初始化

- [x] 1.1 创建 backend/ 目录结构 (cmd/server, internal/handler, internal/service, internal/repository, internal/model, internal/middleware, internal/config)
- [x] 1.2 初始化 Go module (go mod init)，添加依赖 (gin, sqlite, jwt-go, bcrypt, cors)
- [x] 1.3 创建 frontend/ 目录，初始化 Vite + React + TypeScript 项目
- [x] 1.4 安装前端依赖 (tailwindcss, recharts, react-router-dom, axios)
- [x] 1.5 配置 Tailwind CSS
- [x] 1.6 创建 docker-compose.yml 和 Caddyfile

## 2. 数据库层

- [x] 2.1 定义数据模型 (internal/model/user.go, account.go, snapshot.go, event.go)
- [x] 2.2 创建 SQLite 数据库初始化逻辑 (internal/repository/db.go)
- [x] 2.3 实现数据库迁移 (migrations/)
- [x] 2.4 实现 UserRepository (CRUD 操作)
- [x] 2.5 实现 AccountRepository (CRUD + 排序)
- [x] 2.6 实现 SnapshotRepository (含 items 和 events 关联查询)
- [x] 2.7 实现 EventRepository (统计查询)

## 3. 认证模块

- [x] 3.1 实现密码哈希工具 (bcrypt)
- [x] 3.2 实现 JWT Token 生成和验证 (access token + refresh token)
- [x] 3.3 实现认证中间件 (internal/middleware/auth.go)
- [x] 3.4 实现登录限流中间件 (internal/middleware/ratelimit.go)
- [x] 3.5 实现 AuthService (登录、登出、刷新、密码修改)
- [x] 3.6 实现 AuthHandler (POST /auth/setup, /auth/login, /auth/refresh, /auth/logout, /auth/password)
- [x] 3.7 实现首次启动检测逻辑 (GET /auth/status 返回是否需要 setup)

## 4. 用户管理模块

- [x] 4.1 实现 UserService (创建用户、重置密码、列表)
- [x] 4.2 实现 UserHandler (GET/POST /users, PUT /users/:id/password)
- [x] 4.3 实现管理员权限检查中间件

## 5. 账户管理模块

- [x] 5.1 实现 AccountService (CRUD、排序、默认账户初始化)
- [x] 5.2 实现 AccountHandler (GET/POST/PUT/DELETE /accounts, PUT /accounts/reorder)
- [x] 5.3 实现默认账户初始化逻辑 (11 个预设账户)

## 6. 快照模块

- [x] 6.1 实现 SnapshotService (创建、更新、删除、获取最新、历史列表)
- [x] 6.2 实现 SnapshotHandler (GET/POST/PUT/DELETE /snapshots)
- [x] 6.3 实现净资产计算逻辑
- [x] 6.4 实现从上次快照复制逻辑 (GET /snapshots/latest)

## 7. 大事记模块

- [x] 7.1 实现 EventService (CRUD、年度统计)
- [x] 7.2 实现 EventHandler (集成在 snapshot 中 + GET /events/stats)

## 8. 仪表盘模块

- [x] 8.1 实现 DashboardService (汇总数据、趋势、构成、月度增长)
- [x] 8.2 实现 DashboardHandler (GET /dashboard/summary, /trend, /composition, /monthly-growth)
- [x] 8.3 实现年化收益率计算

## 9. 数据导出模块

- [x] 9.1 实现 CSV 导出逻辑
- [x] 9.2 实现 ExportHandler (GET /export/csv)

## 10. 后端集成

- [x] 10.1 配置 CORS 中间件
- [x] 10.2 实现统一错误处理
- [x] 10.3 配置路由 (internal/handler/router.go)
- [x] 10.4 实现配置加载 (internal/config/config.go)
- [x] 10.5 实现 main.go 启动逻辑

## 11. 前端基础

- [x] 11.1 配置路由 (react-router-dom)
- [x] 11.2 创建 API 客户端 (axios 封装，自动刷新 token)
- [x] 11.3 创建认证上下文 (AuthContext)
- [x] 11.4 创建通用组件 (Button, Input, Card, Modal, Loading)
- [x] 11.5 配置响应式断点

## 12. 前端认证页面

- [x] 12.1 实现首次设置页面 (SetupPage)
- [x] 12.2 实现登录页面 (LoginPage)
- [x] 12.3 实现强制修改密码页面 (ChangePasswordPage)
- [x] 12.4 实现路由守卫 (ProtectedRoute)

## 13. 前端账户管理页面

- [x] 13.1 实现账户列表页面 (AccountsPage)
- [x] 13.2 实现账户编辑表单 (AccountForm)
- [x] 13.3 实现拖拽排序功能

## 14. 前端快照页面

- [x] 14.1 实现快照录入页面 (SnapshotFormPage)
- [x] 14.2 实现余额输入组件 (含上次值对比显示)
- [x] 14.3 实现大事记添加组件 (EventForm)
- [x] 14.4 实现净资产实时计算显示
- [x] 14.5 实现快照历史列表页面 (SnapshotsPage)
- [x] 14.6 实现快照详情页面 (SnapshotDetailPage)

## 15. 前端仪表盘页面

- [x] 15.1 实现关键指标卡片组件 (MetricsCards)
- [x] 15.2 实现净资产趋势图 (TrendChart) - 折线图
- [x] 15.3 实现资产构成饼图 (CompositionChart) - 按类型
- [x] 15.4 实现资产构成饼图 (OwnershipChart) - 按所有人
- [x] 15.5 实现资产堆叠面积图 (StackedAreaChart)
- [x] 15.6 实现月度增长柱状图 (MonthlyGrowthChart)
- [x] 15.7 实现账户明细折线图 (AccountTrendChart)
- [x] 15.8 实现时间范围选择器 (TimeRangeSelector)
- [x] 15.9 实现图表导出功能

## 16. 前端设置页面

- [x] 16.1 实现用户管理页面 (UsersPage) - 仅管理员
- [x] 16.2 实现修改密码页面 (ProfilePage)
- [x] 16.3 实现大事记年度统计页面 (EventStatsPage)

## 17. 前端布局和导航

- [x] 17.1 实现主布局 (Layout) 含侧边栏/底部导航
- [x] 17.2 实现响应式导航 (桌面侧边栏/移动端底部)
- [x] 17.3 实现页面标题和面包屑

## 18. 部署配置

- [x] 18.1 编写后端 Dockerfile
- [x] 18.2 编写前端构建脚本 (输出静态文件)
- [x] 18.3 配置 docker-compose.yml (caddy + backend)
- [x] 18.4 配置 Caddyfile (反代 + 静态文件 + HTTPS)
- [x] 18.5 编写部署文档 (README.md)

## 19. 测试和优化

- [x] 19.1 测试认证流程 (登录、登出、刷新、限流)
- [x] 19.2 测试快照录入流程
- [x] 19.3 测试图表显示
- [x] 19.4 测试移动端适配
- [x] 19.5 性能检查 (内存占用 < 150MB)
