## Context

用户需要在 2C2G 轻量服务器上部署家庭资产管理系统。使用者为两人（A 和妻子 B），每月 15 日和 30 日发薪后各录入一次快照。系统需暴露到互联网，因此安全性重要但不需要企业级复杂度。

当前状态：项目从零开始，无现有代码基础。

约束条件：
- 服务器资源有限（2核2GB内存）
- 只有 2 个用户，不需要复杂的用户管理
- 用户希望录入负担最小化
- 需要 HTTPS 保护数据传输

## Goals / Non-Goals

**Goals:**
- 内存占用 < 150MB（应用层）
- 响应时间 < 200ms（常规操作）
- 支持移动端访问（响应式设计）
- 安全可靠（HTTPS、密码哈希、防暴力破解）
- 易于部署和维护（Docker 一键部署）
- 快照录入 5 分钟内完成

**Non-Goals:**
- 不做自动数据拉取（银行API、基金净值爬取）
- 不做复杂的投资建议算法（这是后续模块 B）
- 不做多租户支持
- 不做审计日志
- 不做 2FA（用户觉得太麻烦）

## Decisions

### 1. 后端技术栈：Go + Gin

**选择**: Go 1.21+ with Gin framework

**理由**:
- 编译为单二进制，部署简单
- 内存占用极小（~20MB）
- 性能优秀，无 GC 压力
- 用户偏好

**备选方案**:
- Node.js + Express: 内存占用较高（~100MB+）
- Python + FastAPI: 内存占用中等（~80MB+），需要虚拟环境

### 2. 数据库：SQLite

**选择**: SQLite（文件数据库）

**理由**:
- 零配置，嵌入式
- 数据量小（一年约 300 条记录），SQLite 绰绰有余
- 备份简单（直接复制文件）
- 无独立进程，节省内存

**备选方案**:
- PostgreSQL: 功能强大但占用 100MB+ 内存，过度设计
- MySQL: 同上

### 3. 前端技术栈：React + Vite + Tailwind

**选择**: React 18 + Vite + Tailwind CSS + Recharts

**理由**:
- Vite 构建快，产物小
- Tailwind 原子化 CSS，易于响应式设计
- Recharts 轻量且 React 友好
- 产物为静态文件，由 Caddy 直接提供

### 4. 认证方案：JWT + HttpOnly Cookie

**选择**: 
- Access Token (1h) 存前端内存
- Refresh Token (30d) 存 HttpOnly Cookie
- bcrypt 密码哈希 (cost=12)

**理由**:
- Access Token 短期有效，即使泄露影响有限
- HttpOnly Cookie 防止 XSS 窃取 Refresh Token
- bcrypt 是密码哈希的业界标准

**备选方案**:
- Session Cookie: 需要服务端存储会话，增加复杂度
- 纯 JWT: Refresh Token 存 localStorage 有 XSS 风险

### 5. 用户初始化：首次启动交互设置

**选择**: 首次访问时显示设置页面，创建管理员账户

**理由**:
- 密码不落盘（不存在 .env 或配置文件中）
- 用户体验自然
- 管理员可在设置中添加其他用户

**备选方案**:
- 环境变量配置初始密码: 有泄露风险
- 命令行工具创建用户: 需要额外操作

### 6. 部署方案：Docker Compose + Caddy

**选择**: 
- Docker Compose 编排
- Caddy 反向代理 + 自动 HTTPS

**理由**:
- Docker Compose 一键启动，易于迁移
- Caddy 自动申请 Let's Encrypt 证书，零配置 HTTPS
- 整体内存占用 ~150MB

**备选方案**:
- 裸机部署: 更省内存（~50MB）但维护复杂
- Nginx + Certbot: 需要手动配置证书续期

### 7. 数据模型：快照主表 + 明细表

**选择**: snapshots (主表) + snapshot_items (明细表) 分离

**理由**:
- 灵活查询（按日期、按账户）
- 便于统计聚合
- 支持未来账户增减

**Schema**:
```
users: id, username, password_hash, is_admin, must_change_password, created_at
accounts: id, user_id, name, type, owner, sort_order, is_active, created_at
snapshots: id, user_id, date, note, created_at, created_by
snapshot_items: id, snapshot_id, account_id, balance
events: id, snapshot_id, category, description, amount, created_at
```

## Risks / Trade-offs

### [Risk] SQLite 并发写入限制
→ **Mitigation**: 只有 2 个用户，写入频率极低（每月 2-4 次），不会触发并发问题

### [Risk] 无自动备份
→ **Mitigation**: 
- 提供手动导出功能
- 文档说明定期备份 SQLite 文件的方法
- 可选：添加 cron 定期备份到云存储

### [Risk] Caddy 证书申请失败
→ **Mitigation**: 
- 确保域名 DNS 已解析
- 确保 80/443 端口开放
- 提供 HTTP 回退模式用于调试

### [Risk] 用户忘记密码
→ **Mitigation**: 
- 管理员可重置其他用户密码
- 提供命令行工具重置管理员密码（需要服务器访问权限）

### [Trade-off] 不做数据自动拉取
→ **理由**: 复杂度高，维护成本大，银行 API 不稳定。用户接受手动录入。

### [Trade-off] 不做 2FA
→ **理由**: 用户觉得登录太麻烦。基础安全措施（HTTPS、bcrypt、限流）已足够应对主要威胁。未来可选加入。
