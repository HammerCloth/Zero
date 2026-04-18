#!/usr/bin/env bash
# 首次在 Ubuntu 22.04/24.04 服务器上安装 Docker、Node 22、ufw（需 root 或 sudo）
# 使用前请先阅读 docs/DEPLOYMENT.md；本脚本仅减少复制粘贴，不创建 .env、不 clone 仓库。
# 用法: sudo bash scripts/bootstrap-ubuntu.sh
set -euo pipefail

if [[ "${EUID:-}" -ne 0 ]]; then
  echo "请使用 root 或: sudo bash $0"
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive

echo "==> apt update..."
apt-get update -y

echo "==> 安装 Docker Engine 与 Compose 插件..."
apt-get install -y ca-certificates curl gnupg
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable docker --now

echo "==> 安装 Node.js 22 (NodeSource)..."
curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
apt-get install -y nodejs

echo "==> 安装并配置 ufw（放行 22/80/443）..."
apt-get install -y ufw
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo "==> 版本信息:"
docker --version
docker compose version
node -v
npm -v
ufw status

echo ""
echo "下一步（需用非 root 用户或自行添加 docker 组）："
echo "  usermod -aG docker <你的用户名>"
echo "  然后重新登录 SSH，再执行 git clone、在 zero 目录创建 .env、运行 ./scripts/deploy.sh"
echo "详见 docs/DEPLOYMENT.md"
