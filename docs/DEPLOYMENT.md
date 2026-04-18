# 生产环境部署指南（Docker + Caddy）

本文说明如何把 **Project Zero** 部署到一台远程 Linux 主机（VPS），并让用户通过浏览器访问。  
部署命令均在 **服务器上** 通过 SSH 执行。

---

## 发行版说明（请先读）

| 推荐 | 说明 |
|------|------|
| **Ubuntu 22.04 / 24.04 LTS** | **默认按本文主流程部署**：glibc 新，可安装 **Node 22** 与官方 Docker，文档步骤与下列章节一一对应。 |

| 其它 | 说明 |
|------|------|
| **Debian 12** | 与 Ubuntu 类似，可将下方 `apt` 中发行版代号按 Debian 文档微调，或直接使用 Docker 官方「Debian」安装页。 |
| **Rocky Linux / AlmaLinux 9** | 使用 **附录 A**（yum/dnf、firewalld），勿再使用已 EOL 的 CentOS 8。 |
| **CentOS 7** | **glibc 2.17**，无法安装 NodeSource 的 Node 22，见 **附录 B**。强烈建议换机到 Ubuntu 24.04 或 Rocky 9。 |

**重要目录：** 仓库内 Compose 与 Caddy 配置在 **`zero/`** 下。下文默认你已 `cd` 到 **`zero`**（与 `docker-compose.yml` 同级）。

---

## 0. 你需要先具备什么

| 项目 | 说明 |
|------|------|
| 一台 VPS | 1 核 2G 可用，有公网 IP；**本文主流程以 Ubuntu 24.04 为例** |
| SSH 登录 | 本机能执行 `ssh user@服务器IP` |
| 代码托管 | 仓库已在 GitHub（或 Gitee 等），服务器能 `git clone` |
| 域名（推荐） | 用于 HTTPS；无域名时可用 IP + HTTP 测试（见 §14） |
| 防火墙 | 云安全组 + 本机放行 **22 / 80 / 443** |

---

## 1. 安装 Docker（Ubuntu 24.04）

SSH 登录后执行（来自 [Docker 官方 Ubuntu 安装说明](https://docs.docker.com/engine/install/ubuntu/)，可按官方更新微调）：

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable docker --now
```

验证：

```bash
docker --version
docker compose version
```

当前用户免 `sudo` 使用 Docker（需**重新登录 SSH** 后生效）：

```bash
sudo usermod -aG docker "$USER"
```

---

## 2. 安装 Node.js（Ubuntu，用于构建前端）

在服务器上构建 `frontend/dist` 需要 **Node 20+**（本项目使用 **Node 22** 与 [NodeSource](https://github.com/nodesource/distributions)）：

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
node -v
npm -v
```

缺少 `curl` 时：`sudo apt-get install -y curl`。

**不在服务器上装 Node 的替代做法：** 在你本机执行 `npm ci && npm run build`，将 **`frontend/dist`** 上传到服务器的 `zero/frontend/dist`，然后只执行 `docker compose up`（服务器仅需 Docker）。

---

## 3. 防火墙：ufw（Ubuntu）

- **云控制台**：安全组放行 **TCP 22、80、443**。
- **本机 ufw**：

```bash
sudo apt-get install -y ufw
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status
```

---

## 4. 把代码拉到服务器

```bash
cd /opt
sudo git clone https://github.com/你的用户名/你的仓库名.git
cd 你的仓库名
```

若仓库根下还有 **`zero`** 子目录：

```bash
cd zero
pwd
# 应能看到 docker-compose.yml、Caddyfile、backend/、frontend/
```

以后更新：

```bash
cd /opt/你的仓库名/zero
git pull
```

---

## 5. 生成密钥（不要提交到 Git）

JWT 密钥不是仓库里的文件，由你生成随机串，写入 `.env`。

```bash
openssl rand -hex 48
openssl rand -hex 48
```

两次输出分别作为 **`JWT_ACCESS_SECRET`** 与 **`JWT_REFRESH_SECRET`**，保存在安全处。

---

## 6. 配置环境变量

在 **`zero` 目录**创建 `.env`：

```bash
cd /opt/你的仓库名/zero
nano .env
```

**有域名、HTTPS（推荐）** 示例：

```env
FRONTEND_ORIGIN=https://app.example.com
JWT_ACCESS_SECRET=第一个随机串
JWT_REFRESH_SECRET=第二个随机串
CADDY_SITE=app.example.com
```

```bash
chmod 600 .env
```

- `FRONTEND_ORIGIN`：必须与浏览器地址栏一致（含 `https://`）。
- `CADDY_SITE`：只写域名；Caddy 将申请 Let’s Encrypt（需域名解析到本机且 80/443 可达）。
- 勿将 `.env` 提交到 Git（`zero/.gitignore` 已忽略）。

---

## 7. 构建前端静态资源

```bash
cd /opt/你的仓库名/zero
cd frontend
npm ci
npm run build
cd ..
```

确认存在 **`frontend/dist/`**（含 `index.html`）。

---

## 8. 域名解析（使用 HTTPS 时）

在 DNS 控制台添加 **A 记录**：主机名（如 `app`）→ 服务器**公网 IP**。等待生效后再启动服务。

---

## 9. 启动服务

```bash
cd /opt/你的仓库名/zero
docker compose up -d --build
```

```bash
docker compose ps
docker compose logs -f --tail=100
```

---

## 10. 首次访问与管理员

浏览器打开 **`https://你的域名`**，按引导创建**第一个管理员**（仅无用户时会出现）。

---

## 11. 日常更新（发新版）

```bash
ssh user@服务器IP
cd /opt/你的仓库名/zero
git pull
cd frontend && npm ci && npm run build && cd ..
docker compose up -d --build
```

数据在卷 **`zero_data`**（SQLite），不删卷则数据保留。

---

## 12. 数据备份（建议）

数据库在容器内 `/data/zero.db`，对应卷 **`zero_data`**。升级前请自行备份（导出文件或按运维规范处理卷）。

---

## 13. 常见问题

| 现象 | 排查 |
|------|------|
| 网页打不开 | `docker compose ps`；云安全组与本机 **ufw** 是否放行 80/443；DNS 是否指向本机 IP |
| 登录后 401 / CORS | `FRONTEND_ORIGIN` 是否与浏览器地址完全一致 |
| HTTPS 证书失败 | 域名是否解析到本机；**80** 是否对公网开放（Let’s Encrypt HTTP-01） |

---

## 14. 仅 IP、不配域名（测试）

```env
FRONTEND_ORIGIN=http://你的公网IP
CADDY_SITE=:80
JWT_ACCESS_SECRET=...
JWT_REFRESH_SECRET=...
```

纯 IP 下 Cookie/安全策略与 HTTPS 域名不同，仅建议联调；生产请用**域名 + HTTPS**。

---

## 15. 命令速查（均在 `zero` 目录）

| 目的 | 命令 |
|------|------|
| 构建前端 + 启动（推荐） | `./scripts/deploy.sh` |
| 仅构建前端 | `cd frontend && npm ci && npm run build && cd ..` |
| 启动/重建 | `docker compose up -d --build` |
| 查看日志 | `docker compose logs -f` |
| 停止 | `docker compose down` |

若仓库结构不同，只要进入 **`docker-compose.yml` 所在目录**，步骤顺序不变。

---

## 16. 一键脚本（可选）

无法做到「单条命令从零完成全部」：**`.env` 里的密钥必须你自己生成**，**仓库克隆路径**也因人而异。下面脚本是「能自动的部分」尽量自动化。

| 脚本 | 作用 | 典型用法 |
|------|------|----------|
| **`scripts/bootstrap-ubuntu.sh`** | 仅在**全新 Ubuntu 22.04/24.04** 上**一次性**安装 Docker、Node 22、ufw（需 **root/sudo**） | `sudo bash scripts/bootstrap-ubuntu.sh` |
| **`scripts/deploy.sh`** | 在已有 **`zero/.env`** 的前提下：**构建前端 + `docker compose up`**；可选先 `git pull` | `cd /path/to/zero && ./scripts/deploy.sh` 或 `GIT_PULL=1 ./scripts/deploy.sh` |

**推荐流程：**

1. （仅新机）执行 **`bootstrap-ubuntu.sh`**，然后 `usermod -aG docker <用户>`，**重新登录 SSH**。  
2. **`git clone`** 仓库并进入 **`zero`**，手动创建 **`.env`**（§6）。  
3. 以后每次发版：在 **`zero` 目录**执行 **`./scripts/deploy.sh`**；需要拉代码时 **`GIT_PULL=1 ./scripts/deploy.sh`**。

脚本需在 **`zero` 目录**下执行（或 `bash /绝对路径/zero/scripts/deploy.sh`，脚本会自动定位到 `zero` 根目录）。

---

## 附录 A：Rocky Linux / AlmaLinux / CentOS Stream（dnf）

**Docker（示例，以 Docker 官方文档为准）：**

```bash
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable docker --now
sudo usermod -aG docker "$USER"
```

**Node 22（NodeSource RPM）：**

```bash
curl -fsSL https://rpm.nodesource.com/setup_22.x | sudo bash -
sudo yum install -y nodejs
# 或使用：sudo dnf install -y nodejs
```

**防火墙（firewalld）：**

```bash
sudo systemctl enable firewalld --now
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

若启用 **SELinux** 且挂载卷异常，见 [Docker 与 SELinux](https://docs.docker.com/engine/storage/bind-mounts/#configure-the-selinux-label)。

---

## 附录 B：CentOS 7（glibc 2.17）说明

NodeSource **Node 22** 需要 **glibc ≥ 2.28**，CentOS 7 **无法满足**，会出现 `libc.so.6(GLIBC_2.28)` 等依赖错误。

**可选：**

1. **换系统**（推荐）：Ubuntu 24.04 / Rocky 9 等，再按正文或附录 A 操作。  
2. **试 Node 16 RPM**（可能仍失败或无法满足前端依赖）：  
   `curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -` → `yum install -y nodejs`  
3. **nvm 安装 Node 16**（用户目录）。  
4. **本机构建**：只上传 **`frontend/dist`**，服务器只装 Docker。

---

以上步骤与 **`zero/README.md`** 中 Docker 部署一节互相引用；以本文分步为准。
