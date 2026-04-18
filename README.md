# Project Zero — 家庭资产管家

Go（Gin + SQLite）后端与 Vite + React 前端，适合在 2C2G 机器上以 Docker 部署。

## 本地开发

### 后端

```bash
cd backend
export FRONTEND_ORIGIN=http://localhost:5173
go run ./cmd/server
```

默认监听 `:8080`，数据库 `./data/zero.db`（可设 `DATABASE_PATH`）。

### 前端

```bash
cd frontend
npm install
npm run dev
```

Vite 将 `/api` 代理到 `http://127.0.0.1:8080`，与生产同源 `/api` 行为一致。

### 生产静态资源构建

```bash
cd frontend
npm run build
# 或（含 npm ci）
npm run build:static
```

产物目录：`frontend/dist`。

## 单元测试

前置：已安装 **Go** 与 **Node.js**，前端已执行 `npm install`。

```bash
# 后端（在仓库根目录下的 backend 目录执行；需 CGO 以使用 go-sqlite3）
cd backend
go test ./... -count=1

# 前端
cd frontend
npm test
```

监听模式：`cd frontend && npm run test:watch`。

## Docker 部署（Caddy + 后端）

更完整的分步说明（含服务器准备、`.env`、防火墙与域名）见 **[docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md)**。

1. **构建前端**（Caddy 挂载 `frontend/dist`）：

   ```bash
   cd frontend && npm ci && npm run build && cd ..
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
| 资源 | 部署后 `docker stats` 观察内存，目标 &lt; 150MB 量级（随数据量变化） |

## 目录说明

| 路径 | 说明 |
| --- | --- |
| `backend/` | Go 服务、`Dockerfile`、SQLite 迁移 |
| `frontend/` | React 应用、`scripts/build.sh` 静态构建 |
| `Caddyfile` | 反代 `/api`、静态文件与 SPA `try_files` |
| `docker-compose.yml` | `backend` + `caddy`、数据卷与证书 |

## OpenSpec

活跃变更与归档见 `openspec/changes/`；主规格见 `openspec/specs/`。
