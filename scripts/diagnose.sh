#!/usr/bin/env bash
# 在 zero 目录排查「打不开 / 白屏 / 502」——不修改系统，只打印检查结果
# 用法: cd /path/to/zero && ./scripts/diagnose.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "========== 1. 前端静态文件（最常见：git pull 后未构建，此处为空）=========="
if [[ -f frontend/dist/index.html ]]; then
  echo "[OK] frontend/dist/index.html 存在"
  ls -la frontend/dist | head -8
else
  echo "[失败] 没有 frontend/dist/index.html"
  echo "       修复: cd frontend && npm ci && npm run build && cd .."
  echo "       然后: docker compose up -d --build"
fi

echo ""
echo "========== 2. .env 与 CADDY_SITE（须与浏览器地址一致；www 与根域要都写上或只访问配置的那一个）=========="
if [[ -f .env ]]; then
  grep -E '^(FRONTEND_ORIGIN|CADDY_SITE)=' .env 2>/dev/null | sed 's/=.*/=***/' || true
  RAW_SITE=$(grep -E '^CADDY_SITE=' .env 2>/dev/null | cut -d= -f2- | tr -d '\r' || true)
  if [[ -n "${RAW_SITE}" ]]; then
    FIRST="${RAW_SITE%%,*}"
    FIRST="${FIRST// /}"
    echo "       解析到的第一个站点标识: ${FIRST}"
  fi
else
  echo "[警告] 未找到 .env（Compose 会用默认值 CADDY_SITE=:80，不适合域名 HTTPS）"
fi

echo ""
echo "========== 3. 容器状态 =========="
docker compose ps 2>/dev/null || echo "（无法执行 docker compose，请在 zero 目录、有 docker 权限的机器上运行）"

echo ""
echo "========== 4. Caddy / 后端 最近日志 =========="
docker compose logs caddy --tail 25 2>/dev/null || true
echo "---"
docker compose logs backend --tail 15 2>/dev/null || true

echo ""
echo "========== 5. Caddy 容器能否访问后端（502 时常与此有关）========="
if docker compose exec -T caddy wget -qO- http://backend:8080/healthz 2>/dev/null; then
  echo "(caddy -> backend:8080 正常)"
else
  echo "[失败] 在 caddy 容器内无法访问 http://backend:8080/healthz"
  echo "       检查: docker compose ps 里 backend 是否为 running；勿单独 docker run 脱离 compose 网络"
fi

echo ""
echo "========== 6. 本机对 Caddy 的 HTTP 探测（仅说明本机 80 是否有响应）========="
curl -sI --connect-timeout 3 "http://127.0.0.1/" | head -6 || echo "（本机 127.0.0.1:80 无响应：检查容器是否 up、防火墙、端口映射）"

echo ""
echo "完成。优先对照 §1：若 dist 缺失，网站会空白或无法加载。"
