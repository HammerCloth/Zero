# Project Zero — 家庭资产管家

**Spring Boot 3**（REST + SQLite + Flyway）后端与 **Vite + Vue 3** 前端，适合在 2C2G 机器上以 **Docker + Caddy** 部署。

## 本地开发

### 后端

```bash
cd backend
export FRONTEND_ORIGIN=http://localhost:5173
./mvnw spring-boot:run
```

默认监听 `:8080`，数据库路径由 `DATABASE_PATH` 控制（未设置时见 `application.yml`，通常为 `./data/zero.db`）。

健康检查：`curl -sSf http://127.0.0.1:8080/healthz`

### 前端

```bash
cd frontend-vue
npm install
npm run dev
```

Vite 将 `/api` 代理到 `http://127.0.0.1:8080`，与生产同源 `/api` 行为一致。

### 生产静态资源构建

```bash
cd frontend-vue
npm run build
```

产物目录：`frontend-vue/dist`（Docker 中由 Caddy 挂载）。

## 单元测试与构建校验

前置：已安装 **JDK 21+**、**Node.js 22**（或兼容版本），前端已执行 `npm install`。

```bash
cd backend && ./mvnw test
cd ../frontend-vue && npm run build
```

## Docker 部署（Caddy + 后端）

更完整的分步说明（**以 Ubuntu 24.04 LTS 为主线**，含 Docker、Node 22、ufw、`.env`、域名）见 **[docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md)**；Rocky/Alma 与 CentOS 7 见文档附录。  
一键构建并启动：`cd zero && ./scripts/deploy.sh`（需已配置 `.env`）；新机装依赖：`sudo bash scripts/bootstrap-ubuntu.sh`。

1. **构建前端**（Caddy 挂载 `frontend-vue/dist`）：

   ```bash
   cd frontend-vue && npm ci && npm run build && cd ..
   ```

2. **设置密钥与站点**（勿使用示例默认值）：

   ```bash
   export JWT_ACCESS_SECRET='…'
   export JWT_REFRESH_SECRET='…'
   export FRONTEND_ORIGIN='https://你的域名'
   export CADDY_SITE='你的域名'
   ```

   - `CADDY_SITE`：Caddy 站点地址。默认未设置时为 `:80`（仅 HTTP）。设为域名后，Caddy 会对该域名自动申请 HTTPS（需 80/443 对公网可达）。
   - `FRONTEND_ORIGIN`：须与用户在浏览器访问的源一致（含协议与域名），用于 CORS 与 Cookie。

3. **启动**：

   ```bash
   docker compose up -d --build
   ```

4. **验证**：浏览器打开 `http://localhost`（或你的域名）→ 首次进入会引导创建管理员；登录后测试快照、仪表盘与导出。

## 发布前检查清单（手工）

| 项 | 说明 |
| --- | --- |
| 认证 | 登录、登出、`/refresh` 刷新 access、强制改密、首次 setup |
| 快照 | 新建快照、编辑、列表、详情、删除 |
| 图表 | 仪表盘各图加载、时间范围切换、PNG 导出 |
| 移动端 | 窄屏下底部导航与主布局可用 |
| 资源 | 部署后 `docker stats` 观察内存（随数据量与 JVM 设置变化） |

## 目录说明

| 路径 | 说明 |
| --- | --- |
| `backend/` | Spring Boot 服务、`Dockerfile`、Flyway 迁移 |
| `frontend-vue/` | Vue 3 应用（Vite） |
| `Caddyfile` | 反代 `/api`、静态文件与 SPA `try_files` |
| `docker-compose.yml` | `backend` + `caddy`、数据卷与证书 |

## OpenSpec

活跃变更与归档见 `openspec/changes/`；主规格见 `openspec/specs/`。
