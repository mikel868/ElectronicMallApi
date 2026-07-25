#!/usr/bin/env bash
# =========================================================
#  ElectronicMall 一键部署脚本 (在项目根的 deploy/ 目录运行)
# =========================================================
set -e

cd "$(dirname "$0")"

echo "==> [1/6] 检查 .env 文件"
if [ ! -f .env ]; then
    echo "  未找到 .env，从模板创建..."
    cp .env.example .env
    echo "  ❗ 请编辑 deploy/.env 填入真实密钥后重新运行此脚本"
    exit 1
fi

echo "==> [2/6] 创建数据目录"
mkdir -p data/mysql data/redis data/uploads/avatar data/uploads/file
mkdir -p nginx/certs
mkdir -p ../frontend/dist

echo "==> [3/6] 拷贝 SQL 初始化脚本"
mkdir -p sql/init
cp ../*.sql sql/init/ 2>/dev/null || echo "  项目根无 SQL 文件，跳过"

echo "==> [4/6] 构建后端镜像"
docker compose build app

echo "==> [5/6] 启动服务"
docker compose up -d

echo "==> [6/6] 等待健康检查"
sleep 5
docker compose ps

cat <<'EOF'

✅ 部署完成！

  后端:    http://<服务器IP>/api/   (经 Nginx 反代)
  前端:    http://<服务器IP>/
  文件:    http://<服务器IP>/avatar/  和  /file/

常用命令:
  查看日志:    docker compose logs -f app
  重启后端:    docker compose restart app
  停止全部:    docker compose down
  备份数据:    见 ARCHITECTURE.md 「备份」章节

EOF
