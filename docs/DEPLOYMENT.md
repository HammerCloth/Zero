# 生产环境部署指南（Docker + Caddy）

本文说明如何把 **Project Zero** 部署到一台远程 Linux 主机（VPS），并让用户通过浏览器访问。  
部署命令均在 **服务器上** 通过 SSH 执行；你本地电脑只负责推送代码、SSH 登录（可选）。

---

## 0. 你需要先具备什么

| 项目 | 说明 |
|------|------|
| 一台 VPS | 建议 Ubuntu 22.04/24.04 等，1 核 2G 可用，有公网 IP |
| SSH 登录 | 本机能执行 `ssh user@服务器IP` |
| 代码托管 | 仓库已在 GitHub（或 Gitee 等），服务器能 `git clone` |
| 域名（推荐） | 用于 HTTPS；没有域名时可用 IP + HTTP 做测试（见文末「仅 IP」） |
| 防火墙 | 放行 **80**、**443**（使用 HTTPS 时） |

**重要目录：** 本仓库内所有 Compose 与 Caddy 配置在 **`zero/`** 目录下。下文默认你已 `cd` 到 **`zero`**（与 `docker-compose.yml` 同级）。

---

## 1. 在服务器上安装 Docker

SSH 登录服务器后执行（以 **Ubuntu** 为例；其它系统请参考 Docker 官方文档）。

```bash
# 更新软件源
sudo apt-get update

# 安装依赖
sudo apt-get install -y ca-certificates curl gnupg

# 添加 Docker 官方 GPG 与仓库（若已安装过可跳过）
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

验证：

```bash
docker --version
docker compose version
```

若希望当前用户免 `sudo` 使用 Docker：

```bash
sudo usermod -aG docker "$USER"
# 重新登录 SSH 后生效
```

---

## 2. 安装 Node.js（用于在服务器上构建前端）

Compose 将本机目录 **`frontend/dist`** 挂载进 Caddy，因此需要在 **`zero` 目录下**先执行前端构建。

使用 **Node 20 LTS** 或 **22**（与本地开发尽量一致即可）。可用 [NodeSource](https://github.com/nodesource/distributions) 或 `nvm`，例如：

```bash
# 示例：使用 NodeSource 安装 Node 22（以官方文档为准）
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
node -v
npm -v
```

---

## 3. 把代码拉到服务器

```bash
# 建议放在 /opt 或你的 home 目录
cd /opt
sudo git clone https://github.com/你的用户名/你的仓库名.git
cd 你的仓库名
```

若你的 Git 仓库根目录下才有 `zero` 子目录，请进入 **`zero`**：

```bash
cd zero
pwd
# 应能看到 docker-compose.yml、Caddyfile、backend/、frontend/
```

以后更新版本：

```bash
cd /opt/你的仓库名/zero   # 按你实际路径
git pull
```

---

## 4. 生成密钥（不要提交到 Git）

JWT 密钥**不是**仓库里的某个文件，需要你自己生成随机串，在下一步通过环境变量或 `.env` 注入。

在服务器上执行（示例生成 48 字节十六进制，执行两次得到两个不同的值）：

```bash
openssl rand -hex 48
openssl rand -hex 48
```

分别作为 **`JWT_ACCESS_SECRET`** 和 **`JWT_REFRESH_SECRET`** 保存到安全的地方（密码管理器或仅存在于服务器的 `.env`）。

---

## 5. 配置环境变量

Docker Compose 会读取**当前目录**下的 **`.env`** 文件（若存在），用于替换 `docker-compose.yml` 里的 `${变量名}`。

在 **`zero` 目录**创建 `.env`（路径示例：`/opt/你的仓库名/zero/.env`）：

```bash
cd /opt/你的仓库名/zero
nano .env
```

**有域名、使用 HTTPS（推荐）** 时示例：

```env
# 浏览器实际访问的源（必须带协议，与地址栏一致）
FRONTEND_ORIGIN=https://app.example.com

# 第 4 步生成的两个随机串
JWT_ACCESS_SECRET=这里粘贴第一个
JWT_REFRESH_SECRET=这里粘贴第二个

# Caddy 站点名：只写域名，不要写 https://
CADDY_SITE=app.example.com
```

保存后建议限制权限：

```bash
chmod 600 .env
```

**说明：**

- `FRONTEND_ORIGIN`：用于 CORS 与 Cookie，必须与用户访问地址一致（含 `https://`）。
- `CADDY_SITE`：传给 Caddyfile 的站点块；填域名时 Caddy 会尝试自动申请 Let’s Encrypt 证书（需 80/443 可从公网访问且域名解析到本机）。
- 不要把 `.env` 提交到 Git；确认仓库根目录或 `zero` 下 `.gitignore` 已忽略 `.env`。

---

## 6. 构建前端静态资源

仍在 **`zero` 目录**：

```bash
cd /opt/你的仓库名/zero
cd frontend
npm ci
npm run build
cd ..
```

成功后应存在目录 **`frontend/dist`**（内含 `index.html` 等）。

---

## 7. 防火墙与安全组

- **云厂商控制台**：安全组/防火墙放行入站 **TCP 80、443**。
- **服务器本机**（若启用 ufw）：

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status
```

---

## 8. 域名解析（使用 HTTPS 时）

在域名 DNS 控制台添加 **A 记录**：

- **主机记录**：例如 `app`（完整域名为 `app.example.com`）或 `@`（根域名）。
- **记录值**：你的 VPS **公网 IP**。

等待解析生效（几分钟到数小时不等）。

---

## 9. 启动服务

在 **`zero` 目录**（已存在 `.env` 与 `frontend/dist`）：

```bash
cd /opt/你的仓库名/zero
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
docker compose logs -f --tail=100
```

首次启动 Caddy 申请证书时，日志里可能出现与 ACME 相关的信息；失败时多为 DNS 未指到本机或 80 端口未放行。

---

## 10. 首次访问与管理员账号

在浏览器打开：

- `https://你的域名`（按你在 `.env` 里配置的域名）

按页面引导 **创建第一个管理员账号**（仅在没有用户时会出现）。之后即可登录、录入快照等。

---

## 11. 日常更新（发新版）

```bash
ssh user@服务器IP
cd /opt/你的仓库名/zero
git pull

cd frontend && npm ci && npm run build && cd ..
docker compose up -d --build
```

数据在 Docker 卷 **`zero_data`**（SQLite），只要不删卷，**用户与快照数据会保留**。

---

## 12. 数据备份（建议）

数据库文件在容器内路径为 `/data/zero.db`，对应卷 **`zero_data`**。

备份思路示例：

```bash
docker compose exec backend ls -la /data
# 或停止服务后复制卷（按你运维规范操作）
```

也可定期把卷挂载目录导出（具体以 Docker 存储驱动为准），或使用 `sqlite3` 导出。至少做到：**升级前备份**。

---

## 13. 常见问题

**浏览器打不开**

- `docker compose ps` 是否均为 `running`。
- 安全组与 `ufw` 是否放行 80/443。
- 使用域名时 DNS 是否已指向本机 IP。

**登录后接口 401 / CORS**

- `FRONTEND_ORIGIN` 是否与浏览器地址栏完全一致（协议、域名、端口）。

**HTTPS 证书失败**

- 域名是否解析到本服务器。
- 80 端口是否对外开放（Let’s Encrypt HTTP-01 需要）。

---

## 14. 仅使用 IP、不配域名（测试用）

可将 `.env` 设为仅 HTTP，例如：

```env
FRONTEND_ORIGIN=http://你的公网IP
CADDY_SITE=:80
JWT_ACCESS_SECRET=...
JWT_REFRESH_SECRET=...
```

注意：浏览器对 **纯 IP 的 Cookie/安全策略** 与 HTTPS 域名环境不同，仅建议用于联调；正式使用仍建议 **域名 + HTTPS**。

---

## 15. 命令速查（均在服务器 `zero` 目录）

| 目的 | 命令 |
|------|------|
| 构建前端 | `cd frontend && npm ci && npm run build && cd ..` |
| 启动/重建 | `docker compose up -d --build` |
| 查看日志 | `docker compose logs -f` |
| 停止 | `docker compose down` |

---

若你使用的不是 Ubuntu，或仓库结构不是「根目录下再有 `zero`」**，只需把文中路径改成你机器上 **`docker-compose.yml` 所在目录** 即可，步骤顺序不变。
