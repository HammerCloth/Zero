# Project Zero

家庭资产管理应用，采用：

- `backend/`：Spring Boot 3 + SQLite + Flyway
- `frontend-vue/`：Vue 3 + Vite
- `docker-compose.yml`：`backend` + `caddy`
- MCP：后端内置远程 MCP HTTP endpoint，并通过 OAuth 授权码 + PKCE 让 AI Agent 登录授权

当前线上部署方式适合阿里云服务器：`git pull` 更新代码，再执行前端构建或后端容器更新命令。

## 目录结构

- `backend/`：后端源码、Dockerfile、Flyway 迁移
- `frontend-vue/`：Vue 前端源码与构建产物 `dist/`
- `Caddyfile`：静态站点与 `/api`、`/mcp`、`/oauth`、OAuth discovery 反代
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

## 礼金簿

网页端的“礼金”页面用于记录自己实际承担的礼金支出，不与资产快照或大事记混用，因此不会重复影响账户余额或净资产统计。

- 先维护可枚举的礼金对象；每条记录通过对象 ID 关联历史，而不是依赖手工输入姓名。
- 每笔记录包含对象、关系、场合、日期、实际承担金额、支付方式与备注。
- 支持按年度查看支出、笔数、涉及对象和按场合统计；可查看某一对象的完整往来历史。
- 已有关联记录的对象采用“停用”而非删除，确保历史记录不丢失。

该功能由 Flyway 迁移 `V4__gift_records.sql` 创建以下表：

```text
gift_recipients
gift_records
```

线上发布包含此功能时，后端启动会自动执行 V4 迁移；发布前仍建议备份数据库。

## MCP 与 AI 客户端授权

后端提供远程 MCP endpoint：

```text
POST /mcp
```

MCP 访问使用标准 OAuth 授权码 + PKCE 流程。AI Agent 第一次连接时会通过 discovery 找到授权入口，打开浏览器登录 Project Zero 已有账号，登录成功后 Agent 使用 authorization code 换取 access token 和 refresh token。MCP 不开放新用户注册；没有现有账号会授权失败。

相关端点：

```text
GET  /.well-known/oauth-protected-resource
GET  /.well-known/oauth-authorization-server
POST /oauth/register
GET  /oauth/authorize
POST /oauth/authorize
POST /oauth/token
POST /oauth/revoke
POST /mcp
```

MCP 当前提供 4 个 tool：

```text
get_analysis_guide
list_snapshots
get_asset_snapshot
get_major_financial_events
```

说明：

- `get_analysis_guide` 告诉调用方 AI 当前系统是基于资产快照的数据模型，以及数据限制。
- `list_snapshots` 列出当前用户已有快照。
- `get_asset_snapshot` 获取最新、指定 ID 或指定日期的资产快照。
- `get_major_financial_events` 按关联快照日期查询大事记；不按 `events.created_at` 查询。
- AI 客户端拿到的是专用 `mcp_access` token，只能访问 `/mcp`，不能访问普通 `/api/**`。

网页端新增“AI 客户端”页面，用于查看和撤销已授权的 AI Agent。后端管理 API：

```text
GET    /api/v1/oauth/authorizations
DELETE /api/v1/oauth/authorizations/{id}
DELETE /api/v1/oauth/authorizations
```

MCP 授权功能对应 Flyway 迁移 `V3__oauth_mcp.sql`，会创建：

```text
oauth_clients
oauth_authorization_codes
oauth_refresh_tokens
```

线上发布前建议先备份数据库。

### 安装到 AI 客户端

下面示例假设线上域名是：

```text
https://app.example.com
```

实际使用时替换为你的 `CADDY_SITE` 域名。远程 MCP URL 固定为：

```text
https://app.example.com/mcp
```

#### Codex

Codex CLI 和 IDE extension 共享 `config.toml` 中的 MCP 配置。可以编辑用户级配置：

```bash
mkdir -p ~/.codex
nano ~/.codex/config.toml
```

加入：

```toml
[mcp_servers.project_zero]
url = "https://app.example.com/mcp"
```

然后执行 OAuth 登录：

```bash
codex mcp login project_zero
```

也可以在 Codex TUI 中输入：

```text
/mcp
```

查看 MCP server 状态并触发登录。Codex 会打开浏览器，登录 Project Zero 已有账号后保存 OAuth token。官方配置说明见 [OpenAI Codex MCP docs](https://developers.openai.com/codex/mcp)。

#### Claude Code

添加 HTTP MCP server：

```bash
claude mcp add --transport http project-zero https://app.example.com/mcp
```

然后执行 OAuth 登录：

```bash
claude mcp login project-zero
```

或者进入 Claude Code 交互会话后输入：

```text
/mcp
```

在 MCP 面板里选择 `project-zero` 并完成浏览器登录。服务器返回 `401` 时会通过 `WWW-Authenticate` 指向 OAuth discovery，Claude Code 会按 OAuth 2.0 流程完成授权并自动刷新 token。远程 SSH 环境下可使用：

```bash
claude mcp login project-zero --no-browser
```

它会打印授权 URL，浏览器登录后把回调 URL 粘回终端。官方说明见 [Claude Code MCP docs](https://docs.anthropic.com/en/docs/claude-code/mcp)。

#### Claude Desktop

如果你的 Claude Desktop 版本支持远程 HTTP MCP server，可以使用与 Claude Code 相同的远程地址：

```text
https://app.example.com/mcp
```

在客户端 MCP 设置里新增 HTTP server，名称建议用 `project-zero`。首次调用或在 MCP 管理界面中触发认证时，客户端会打开浏览器走 OAuth 登录。不同 Claude Desktop 版本的 MCP 配置入口可能不同；如果界面没有远程 HTTP MCP 配置项，优先使用 Claude Code 的 `claude mcp add --transport http ...` 方式。

#### 验证

授权成功后，可以让客户端询问：

```text
请调用 Project Zero MCP，列出当前可用快照，并说明这个资产系统的数据模型限制。
```

如果客户端显示未授权或没有 tools：

- 确认 `https://你的域名/mcp` 能访问到后端，而不是前端页面。
- 确认 `https://你的域名/.well-known/oauth-protected-resource` 返回 JSON。
- 确认线上 Caddyfile 已包含 `/mcp`、`/oauth/*` 和 `/.well-known/*` 反代。
- 在网页端“AI 客户端”页面撤销旧授权后，重新执行客户端登录命令。

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
- `curl -sSf http://127.0.0.1/.well-known/oauth-authorization-server`
- 登录一次，确认读写正常
- 如本次涉及 MCP，确认 AI 客户端页面可打开，并验证 `/mcp` 能通过 OAuth 授权后访问

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
