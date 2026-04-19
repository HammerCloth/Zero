## 1. 后端项目初始化

- [x] 1.1 创建 Spring Boot 3 Maven 项目骨架
- [x] 1.2 配置 pom.xml 依赖（Spring Boot、MyBatis、SQLite、Flyway、JJWT、Security）
- [x] 1.3 配置 application.yml（端口、数据库、JWT 密钥占位符）
- [x] 1.4 创建 Flyway 迁移脚本 V1__init.sql（users、accounts、snapshots、snapshot_items、events 表）
- [x] 1.5 配置 MyBatis（MapperScan、驼峰映射）
- [ ] 1.6 验证应用启动和数据库初始化（手测 / 本地运行）

## 2. 后端安全配置

- [x] 2.1 创建 JwtService（生成/验证 access_token 和 refresh_token）
- [x] 2.2 创建 JwtAuthenticationFilter（从 Authorization 头提取并验证 JWT）
- [x] 2.3 配置 SecurityConfig（Stateless、CORS、公开端点、受保护端点）
- [x] 2.4 创建 GlobalExceptionHandler（统一错误响应格式）
- [x] 2.5 实现登录频率限制（RateLimiter 或内存缓存）

## 3. 后端 Auth 模块

- [x] 3.1 创建 User 实体和 UserMapper
- [x] 3.2 创建 AuthService（needsSetup、setup、login、refresh、logout、me、changePassword）
- [x] 3.3 创建 AuthController（端点：`/api/v1/auth/**`）
- [x] 3.4 实现 refresh_token Cookie 设置（HttpOnly、Secure、SameSite）
- [ ] 3.5 用 curl/Postman 验证完整认证流程

## 4. 后端 User 模块

- [x] 4.1 创建 UserService（list、create、resetPassword）
- [x] 4.2 创建 UserController（`/api/v1/users/**`）
- [x] 4.3 添加 @PreAuthorize 管理员权限校验
- [ ] 4.4 验证用户管理 API

## 5. 后端 Account 模块

- [x] 5.1 创建 Account 实体和 AccountMapper
- [x] 5.2 创建 AccountService（list、create、update、deactivate、reorder）
- [x] 5.3 创建 AccountController（`/api/v1/accounts/**`）
- [x] 5.4 实现初始化时默认账户创建
- [ ] 5.5 验证账户 API

## 6. 后端 Snapshot 模块

- [x] 6.1 创建 Snapshot、SnapshotItem、Event 实体和 Mapper
- [x] 6.2 创建 SnapshotService（list、get、getLatest、create、update、delete）
- [x] 6.3 创建 SnapshotController（`/api/v1/snapshots/**`）
- [x] 6.4 实现负债账户余额自动取负
- [ ] 6.5 验证快照 API

## 7. 后端 Dashboard 模块

- [x] 7.1 创建 DashboardService（summary、trend、composition、monthlyGrowth）
- [x] 7.2 创建 DashboardController（`/api/v1/dashboard/**`）
- [x] 7.3 实现年化收益率计算
- [ ] 7.4 验证仪表盘 API

## 8. 后端 Event/Export 模块

- [x] 8.1 创建 EventService（statsByYear）（统计实现在 Mapper + EventController）
- [x] 8.2 创建 EventController（`/api/v1/events/stats`）
- [x] 8.3 创建 ExportService（生成 CSV）
- [x] 8.4 创建 ExportController（`/api/v1/export/csv`，UTF-8 BOM）
- [ ] 8.5 验证统计和导出 API

## 9. 后端 Docker 配置

- [x] 9.1 创建多阶段 Dockerfile（Maven 构建 + JRE 运行）
- [x] 9.2 配置 JVM 内存限制（-Xmx256m）
- [x] 9.3 更新 docker-compose.yml（Spring backend；Caddy 挂载 `frontend-vue/dist`）
- [ ] 9.4 本地 Docker 构建和运行验证

## 10. 前端项目初始化

> 新版前端目录：`frontend-vue/`（`npm run dev` / `npm run build`）。原 `frontend/` 为历史 React，可保留参考。

- [x] 10.1 创建 Vue 3 + Vite + TypeScript 项目
- [x] 10.2 安装依赖（Naive UI、Pinia、Vue Router、Axios）
- [x] 10.3 配置 Naive UI 按需导入（unplugin-vue-components + NaiveUiResolver；`main.ts` 全量 naive 以简化 Message 等）
- [x] 10.4 配置 Vue Router（路由定义、守卫）
- [x] 10.5 配置 Pinia（auth store）
- [x] 10.6 配置 Axios 实例（baseURL、401 刷新）
- [x] 10.7 验证项目启动（`npm run build` 已通过）

## 11. 前端 API 层和认证

- [x] 11.1 创建 TypeScript 类型定义（User、Account、Snapshot 等）
- [x] 11.2 封装 Axios 实例（Authorization 头、401 自动刷新）
- [x] 11.3 创建 auth store（user、isLoggedIn、login、logout、init）
- [x] 11.4 实现路由守卫（未登录跳转、mustChangePassword 跳转）
- [x] 11.5 创建 auth API 函数（status、setup、login、refresh、logout、me、changePassword）

## 12. 前端 Auth 页面

- [x] 12.1 创建 SetupPage（初始化管理员）
- [x] 12.2 创建 LoginPage（登录表单）
- [x] 12.3 创建 ChangePasswordPage（修改密码）
- [ ] 12.4 验证完整认证流程（浏览器手测）

## 13. 前端布局

- [x] 13.1 创建 AppLayout（侧边栏 + 主内容区）
- [x] 13.2 实现响应式布局（桌面侧栏 + 移动抽屉菜单）
- [x] 13.3 创建导航菜单（总览、快照、账户、大事记）
- [x] 13.4 实现管理员专属导航项（用户管理）

## 14. 前端 Dashboard 页面

- [x] 14.1 创建 dashboard API 函数
- [x] 14.2 创建 DashboardPage 骨架
- [x] 14.3 实现指标卡片组件
- [x] 14.4 实现净资产趋势图（折线图 + 范围切换）
- [x] 14.5 实现资产构成饼图（类型 + 归属）
- [x] 14.6 实现月度增长柱状图
- [ ] 14.7 实现资产堆叠面积图（当前以折线面积为主，未单独做堆叠多系列）
- [ ] 14.8 实现账户明细趋势图（带筛选）（页面内说明占位，可后续迭代）
- [x] 14.9 实现 CSV 导出按钮

## 15. 前端 Snapshot 页面

- [x] 15.1 创建 snapshot API 函数
- [x] 15.2 创建 SnapshotsPage（列表页）
- [x] 15.3 创建 SnapshotDetailPage（详情页）
- [x] 15.4 创建 SnapshotFormPage（新建/编辑页）
- [x] 15.5 实现账户余额表单组件
- [x] 15.6 实现大事记表单组件（添加/删除行）
- [ ] 15.7 验证快照增删改查流程（手测）

## 16. 前端 Account 页面

- [x] 16.1 创建 account API 函数
- [x] 16.2 创建 AccountsPage（列表 + 新建/编辑对话框）
- [x] 16.3 实现拖拽排序（vuedraggable）
- [x] 16.4 实现停用功能
- [ ] 16.5 验证账户管理流程（手测）

## 17. 前端 Event 页面

- [x] 17.1 创建 event API 函数
- [x] 17.2 创建 EventStatsPage（年度统计表格）
- [x] 17.3 实现年份切换
- [ ] 17.4 验证大事记统计（手测）

## 18. 前端 User 页面

- [x] 18.1 创建 user API 函数
- [x] 18.2 创建 UsersPage（用户列表 + 新建/重置密码对话框）
- [ ] 18.3 验证用户管理流程（手测）

## 19. 前端 Docker 配置

- [x] 19.1 更新构建产物路径（`docker-compose` → `frontend-vue/dist`）
- [x] 19.2 验证前端 dist 生成（`npm run build`）
- [x] 19.3 更新 Caddyfile（如有需要）（无需变更，`/api` 反代不变）

## 20. 集成测试和部署

- [ ] 20.1 本地 docker compose up 完整测试
- [ ] 20.2 验证所有功能正常
- [ ] 20.3 推送 spring-backend 分支
- [ ] 20.4 服务器部署验证
- [ ] 20.5 重置数据卷并完成首次初始化
