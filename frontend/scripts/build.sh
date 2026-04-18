#!/usr/bin/env sh
set -euf
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ ! -d node_modules ]; then
  npm ci
fi
npm run build
echo "Static files: $ROOT/dist"
