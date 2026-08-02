#!/usr/bin/env bash
# =========================================================
#  ElectronicMall 服务器初始化脚本（在服务器上执行一次即可）
#
#  做的事：
#    1. 加 4G swap（2G 服务器必做，避免 OOM）
#    2. 配置 Docker 镜像加速器（解决 docker.io 拉镜像超时）
#    3. 重启 Docker 让配置生效
#
#  使用：在 deploy/ 目录执行 bash setup.sh
# =========================================================
set -e

echo "==> [1/3] 配置 swap"
if swapon --show | grep -q swapfile; then
    echo "  swap 已存在，跳过"
else
    fallocate -l 4G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
    sysctl -w vm.swappiness=10 >/dev/null
    grep -q '^vm.swappiness' /etc/sysctl.conf || echo 'vm.swappiness=10' >> /etc/sysctl.conf
    echo "  ✓ 已配置 4G swap"
fi

echo "==> [2/3] 配置 Docker 镜像加速器"
mkdir -p /etc/docker
cp docker-daemon.json /etc/docker/daemon.json
echo "  ✓ 已写入 /etc/docker/daemon.json"
echo "  当前加速器："
cat /etc/docker/daemon.json | grep -A 10 registry-mirrors

echo "==> [3/3] 重启 Docker"
systemctl daemon-reload
systemctl restart docker
echo "  ✓ Docker 已重启"

echo ""
echo "==> 验证中（拉一个测试镜像）"
if docker pull hello-world 2>&1 | tail -3; then
    echo ""
    echo "✅ 初始化完成！接下来运行：./deploy.sh"
else
    echo ""
    echo "❌ 公共加速器仍不通，请改用阿里云专属加速器："
    echo "   1. 浏览器登录 https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors"
    echo "   2. 复制你的专属加速地址（形如 https://xxxxxx.mirror.aliyuncs.com）"
    echo "   3. vim /etc/docker/daemon.json"
    echo "   4. 把 registry-mirrors 第一行替换为你的专属地址"
    echo "   5. systemctl restart docker"
fi
