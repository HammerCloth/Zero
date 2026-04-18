#!/usr/bin/env bash
# 在 zero 目录下完成：可选 git pull → 构建前端 → docker compose up
# 用法: ./scripts/deploy.sh
#       GIT_PULL=1 ./scripts/deploy.sh   # 先 git pull 再部署
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "错误：未找到 $ROOT/.env"
  echo "请先按 docs/DEPLOYMENT.md 创建并填写 FRONTEND_ORIGIN、JWT_ACCESS_SECRET、JWT_REFRESH_SECRET、CADDY_SITE"
  exit 1
fi

if [[ ! -d frontend ]]; then
  echo "错误：未找到 frontend 目录（应在 zero 目录下执行本脚本）"
  exit 1
fi

if [[ "${GIT_PULL:-0}" == "1" ]]; then
  echo "==> git pull 中..."
  git pull
fi

echo "==> 构建前端 (npm ci && npm run build)..."
(cd frontend && npm ci && npm run build)

echo "==> 启动 / 更新 Docker 服务..."
docker compose up -d --build

echo "==> 完成。查看: docker compose ps"
echo "    日志: docker compose logs -f --tail=50"
