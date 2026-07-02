# Project Zero

家庭资产管理应用，采用：

- `backend/`：Spring Boot 3 + SQLite + Flyway
- `frontend-vue/`：Vue 3 + Vite
- `docker-compose.yml`：`backend` + `caddy`

当前线上部署方式适合阿里云服务器：`git pull` 更新代码，再执行前端构建或后端容器更新命令。

## 目录结构

- `backend/`：后端源码、Dockerfile、Flyway 迁移
- `frontend-vue/`：Vue 前端源码与构建产物 `dist/`
- `Caddyfile`：静态站点与 `/api` 反代
- `docker-compose.yml`：生产容器编排
- `scripts/deploy.sh`：一键构建前端并更新容器
- `docs/DEPLOYMENT.md`：更完整的部署说明

## 线上部署前提

服务器已具备以下条件：

- 已安装 Docker 与 Docker Compose
- 项目目录已 `git clone`
- 根目录存在 `.env`
- 数据库使用 Docker 卷 `zero_data`，容器内路径为 `/data/zero.db`

后端默认不会把数据库放在仓库里，而是放在 Docker 卷中；只要你不执行 `docker compose down -v` 或手工删除卷，现有数据库文件会保留。

## 本地开发

后端：

```bash
cd backend
export FRONTEND_ORIGIN=http://localhost:5173
./mvnw spring-boot:run
```

前端：

```bash
cd frontend-vue
npm install
npm run dev
```

构建前端：

```bash
cd frontend-vue
npm run build
```

## 阿里云线上更新命令

以下命令默认在项目根目录 `zero/` 下执行。

### 1. 只更新 Vue 前端

适用场景：只改了 `frontend-vue/`，不需要重发后端。

```bash
git pull
cd frontend-vue
npm ci
npm run build
cd ..
docker compose restart caddy
```

说明：

- 前端静态文件直接输出到 `frontend-vue/dist/`
- Caddy 挂载这个目录并对外提供页面
- 这个流程不会重建后端容器，也不会动当前数据库

### 2. 只更新后端（保留当前数据库）

适用场景：只改了 `backend/`，希望保留当前线上数据。

```bash
git pull
docker compose build backend
docker compose up -d backend
```

说明：

- 这组命令会更新 `backend` 容器
- `zero_data` 卷会继续挂载，所以不会删除当前 `zero.db`
- `caddy` 不需要重建

重要说明：

- “保留当前数据库”指的是：不删库、不重置卷、不替换现有 `zero.db`
- 但如果新版本后端包含新的 Flyway migration，后端启动时仍会自动执行迁移并修改数据库结构
- 如果你这次明确要求“连表结构都不要动”，先检查 `backend/src/main/resources/db/migration/` 是否新增了 SQL 文件；有新增时，先备份数据库再发版

### 3. 前后端一起更新

```bash
git pull
cd frontend-vue
npm ci
npm run build
cd ..
docker compose up -d --build
```

如果你平时就按这个方式发版，也可以直接用：

```bash
GIT_PULL=1 ./scripts/deploy.sh
```

## 建议的发版检查

前端更新后：

- 打开首页与登录页
- 检查 `/api` 请求是否正常
- 检查手机端页面是否无横向溢出

后端更新后：

- `docker compose logs -f --tail=100 backend`
- `curl -sSf http://127.0.0.1:8080/healthz`
- 登录一次，确认读写正常

## 不要这样做

下面这些操作可能影响当前数据库：

- `docker compose down -v`
- 手工删除 `zero_data` 卷
- 手工删除容器内 `/data/zero.db`
- 在未确认 migration 的情况下直接发布结构变更

## 补充说明

- 后端数据库配置见 `backend/src/main/resources/application.yml`
- 当前 SQLite 连接为 `jdbc:sqlite:${DATABASE_PATH:./data/zero.db}`
- 生产环境在 Compose 中通过 `DATABASE_PATH=/data/zero.db` 固定到数据卷

如需更完整的 Ubuntu / 域名 / HTTPS / 防火墙说明，请看 [docs/DEPLOYMENT.md](/Users/siyixiong/IdeaProjects/curcor project/zero/docs/DEPLOYMENT.md)。
