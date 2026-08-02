# ElectronicMall 部署指南（小白向）

> **目标读者**：第一次上线 Java 项目的同学，能看懂命令就行。
> **目标环境**：阿里云/腾讯云 2 核 2G Ubuntu 服务器。
> **预计耗时**：1.5 ~ 2 小时（含等服务器启动）。
> **最终效果**：浏览器访问 `http://你的IP/swagger-ui/index.html` 能看到所有 API 文档并能在线调试。

---

## 目录

1. [整体流程概览](#1-整体流程概览)
2. [第一步：买服务器](#2-第一步买服务器)
3. [第二步：连接服务器](#3-第二步连接服务器)
4. [第三步：服务器初始化（5 条命令）](#4-第三步服务器初始化5-条命令)
5. [第四步：装 Docker](#5-第四步装-docker)
6. [第五步：把代码弄到服务器](#6-第五步把代码弄到服务器)
7. [第六步：准备数据库脚本](#7-第六步准备数据库脚本)
8. [第七步：填配置文件](#8-第七步填配置文件)
9. [第八步：一键启动](#9-第八步一键启动)
10. [第九步：验证上线](#10-第九步验证上线)
11. [常用运维命令](#11-常用运维命令)
12. [出问题怎么办](#12-出问题怎么办)
13. [安全加固（强烈建议做）](#13-安全加固强烈建议做)

---

## 1. 整体流程概览

```
买服务器 → 连上去 → 装 Docker → 上传代码 → 改密码 → 启动 → 访问
   10min     5min      10min      15min      5min    10min   5min
```

最终服务器上会跑起来 4 个 Docker 容器：

```
┌─ Nginx (对外开放 80 端口)
│   ↓ 内部转发
├─ Spring Boot 应用 (9191 端口，只内网)
│   ↓
├─ MySQL (3306，只内网)
└─ Redis (6379，只内网)
```

---

## 2. 第一步：买服务器

### 推荐选项

| 厂商 | 推荐产品 | 价格 | 适用场景 |
|------|---------|------|---------|
| **腾讯云** | 轻量应用服务器 2C2G | 学生 ¥10/月，普通 ~¥50/月 | **推荐**，简单 |
| **阿里云** | ECS 经济型 e 实例 2C2G | ~¥40-80/月 | 也可以 |
| **境外**（Vultr / 搬瓦工） | 1C2G | ~$5/月 | 不想备案 |

### 关键选择

- **操作系统**：选 **Ubuntu 22.04 LTS**（不要选 CentOS，已停止维护）
- **地域**：国内用户访问选北上广深；不想备案选香港/新加坡
- **带宽**：3-5Mbps 够用
- **磁盘**：默认 40-50GB SSD 够用

### 买完后必做的事

云厂商控制台会给你 3 个东西，记下来：

```
服务器公网 IP：    例 123.45.67.89
初始登录密码：     例 abcd1234
用户名：           ubuntu  (或 root)
```

**⚠️ 务必在控制台开放安全组端口**：

| 端口 | 用途 | 必须开吗 |
|------|------|---------|
| **22** | SSH 远程登录 | ✅ 必须 |
| **80** | HTTP 网页访问 | ✅ 必须 |
| 443 | HTTPS（以后配域名再开） | 暂不开 |
| 3306/6379/9191 | 数据库/Redis/应用 | ❌ **不要开**！只让内网用 |

---

## 3. 第二步：连接服务器

### Windows 用户（推荐用 PowerShell）

按 `Win+R`，输入 `powershell` 回车。然后：

```powershell
ssh root@你的服务器IP
# 例：ssh root@123.45.67.89
# 第一次连接问 yes/no，输入 yes
# 提示密码时输入初始密码（输入时光标不动是正常的）
```

### 如果用 ubuntu 用户名

```powershell
ssh ubuntu@你的服务器IP
# 登录后用 sudo -i 切到 root
sudo -i
```

**后面的命令默认你在 root 用户下执行。** 命令提示符是 `#`。

---

## 4. 第三步：服务器初始化（5 条命令）

### 4.1 改密码

```bash
passwd
# 输入新密码两次（建议 16 位以上，含大小写数字符号）
```

### 4.2 更新系统（约 2-3 分钟）

```bash
apt update && apt upgrade -y
```

### 4.3 设置时区为上海

```bash
timedatectl set-timezone Asia/Shanghai
date    # 验证一下
```

### 4.4 ⭐ 加 swap（**2G 服务器必做**，否则可能 OOM）

```bash
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
sysctl -w vm.swappiness=10
echo 'vm.swappiness=10' >> /etc/sysctl.conf

free -h
# 应该看到 Swap 一行有 4.0Gi
```

### 4.5 装几个常用工具

```bash
apt install -y git curl wget vim
```

---

## 5. 第四步：装 Docker

**一键脚本**（最简单）：

```bash
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```

等 1-2 分钟，看到类似 `Docker version 24.x.x` 就成功了。

验证：

```bash
docker --version
docker compose version    # 注意是 docker compose 不是 docker-compose
```

两个命令都能输出版本号就行。

> **如果第二个命令报错**：说明 Docker 版本太老。手动装最新版：
> ```bash
> curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
> echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
> apt update && apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
> ```

---

## 6. 第五步：把代码弄到服务器

### 方式 A：用 git（推荐，前提是项目已上传到 GitHub/Gitee）


```bash
cd /opt
git clone https://github.com/你的用户名/ElectronicMallApi.git
cd ElectronicMallApi
```

### 方式 B：本地上传（没有 git 仓库时）

在你**本地电脑的 PowerShell**：

```powershell
cd "D:\buy\ElectronicMallApi - idea1"

# 打包（排除 target 等大文件）
tar -czf mall.tar.gz --exclude=target --exclude=.git --exclude=*.iml .

# 上传到服务器
scp mall.tar.gz root@你的服务器IP:/opt/

# 切回服务器 SSH 会话
ssh root@你的服务器IP
```

服务器上：

```bash
mkdir -p /opt/ElectronicMallApi
cd /opt/ElectronicMallApi
tar -xzf /opt/mall.tar.gz
cd /opt/ElectronicMallApi
```

验证代码到位：

```bash
ls /opt/ElectronicMallApi/deploy/
# 应该看到：docker-compose.yml  deploy.sh  nginx  .env.example
```

---

## 7. 第六步：准备数据库脚本

⚠️ **项目里只有 `seckill_voucher.sql`（秒杀表），缺主业务表（user/goods/order/cart 等）。**

### 你需要从本地数据库导出一份完整 schema

本地电脑（已装 MySQL）打开 cmd：

```bash
mysqldump -u root -p --no-data --routines --triggers electronic_mall > schema.sql
# 输入密码后会在当前目录生成 schema.sql，约 10-50KB
```

把这个 `schema.sql` 上传到服务器项目根目录：

```bash
# 本地 PowerShell
scp schema.sql root@8.163.78.177:/opt/ElectronicMallApi/
```

服务器上验证：

```bash
ls /opt/ElectronicMallApi/*.sql
# 应该看到 schema.sql 和 seckill_voucher.sql
```

> **为什么需要这步**：MySQL 容器第一次启动时会自动执行项目根下所有 `.sql`（除了 `ai_order.sql`，因为 AI 已下线），自动建表。没这步启动会成功但 API 调用全报"表不存在"。

---

## 8. 第七步：填配置文件

服务器上：

```bash
cd /opt/ElectronicMallApi/deploy
cp .env.example .env
vim .env
```

按 `i` 进入插入模式，**至少改以下 4 处**（其他可保留默认）：

```ini
# 1. MySQL root 密码（改成强密码，至少 16 位）
MYSQL_ROOT_PASSWORD=改成强密码_例如_Mall@RootPwd2025

# 2. 应用数据库账号密码
MYSQL_USER=mall_app
MYSQL_PASSWORD=改成另一个强密码

# 3. Redis 密码
REDIS_PASSWORD=改成强密码_例如_Redis@Pwd2025

# 4. JWT 密钥（至少 32 位随机字符串）
JWT_SECRET=随便敲一串32位以上的字符abc123xyz789

# 5. CORS 白名单（设为服务器 IP）
CORS_ALLOWED_ORIGINS=http://你的服务器IP
```

改完按 `Esc`，输入 `:wq` 回车保存退出。

> 💡 强密码生成：在 vim 命令行外执行 `openssl rand -base64 24`，复制输出。

**改完后必须改文件权限**（防止别的用户读到密码）：

```bash
chmod 600 .env
```

---

## 9. 第八步：一键启动

```bash
cd /opt/ElectronicMallApi/deploy
./deploy.sh
```

脚本会自动做 6 件事：
1. ✅ 检查 .env
2. ✅ 创建数据目录
3. ✅ 拷贝 SQL 初始化脚本
4. ✅ 构建后端镜像（**这步最慢，约 5-10 分钟**，因为要下 Maven 依赖）
5. ✅ 启动所有容器
6. ✅ 显示运行状态

看到 `✅ 部署完成！` 就成功了。

---

## 10. 第九步：验证上线

### 10.1 看容器状态

```bash
docker compose ps
```

应该看到 4 个容器都是 `Up` 状态：

```
NAME       IMAGE                       STATUS         PORTS
em-app     electronic-mall-api:latest  Up 30 seconds
em-mysql   mysql:8.0                   Up (healthy)
em-nginx   nginx:1.27-alpine           Up 30 seconds  0.0.0.0:80->80/tcp
em-redis   redis:7-alpine              Up 30 seconds
```

如果有 `Restarting` 或 `Exited`，看 [出问题怎么办](#12-出问题怎么办)。

### 10.2 看后端日志（等它启动完）

```bash
docker compose logs -f app
```

看到 `Started ElectronicMallApplication in xx seconds` 就是启动完成。按 `Ctrl+C` 退出日志查看。

### 10.3 浏览器访问

打开**你自己电脑**的浏览器：

| 地址 | 应该看到 |
|------|---------|
| `http://你的IP/swagger-ui/index.html` | ✅ Swagger API 文档（最关键验证） |
| `http://你的IP/actuator/health` | `{"status":"UP"}` |
| `http://你的IP/` | 404（正常，没前端 dist） |

✅ **能看到 Swagger 文档就上线成功了！可以发链接给朋友了。**

---

## 11. 常用运维命令

所有命令都在 `/opt/ElectronicMallApi/deploy` 目录下执行。

```bash
# 看所有容器状态
docker compose ps

# 看实时日志
docker compose logs -f app      # 后端
docker compose logs -f nginx    # Nginx
docker compose logs -f mysql    # MySQL

# 重启某个服务
docker compose restart app

# 停掉所有服务（不删数据）
docker compose down

# 重新构建并启动（改了代码后）
cd /opt/ElectronicMallApi
git pull                      # 拉最新代码
cd deploy
docker compose up -d --build app

# 进容器里看
docker exec -it em-app sh
docker exec -it em-mysql mysql -uroot -p

# 看内存占用
docker stats --no-stream

# 看磁盘
df -h
```

---

## 12. 出问题怎么办

### Q1：`docker compose up` 报 `port is already allocated`

80 端口被其他程序占用。

```bash
lsof -i:80
# 看到 PID 后 kill
kill -9 <PID>
```

### Q2：app 容器一直 Restarting

最常见是连不上数据库。看日志：

```bash
docker compose logs app | tail -50
```

常见原因：
- **密码错**：`.env` 里 `MYSQL_PASSWORD` 和 `MYSQL_ROOT_PASSWORD` 必须不同
- **数据库没启动完就连接**：稍等 30 秒再 `docker compose restart app`
- **schema.sql 报错**：`docker compose logs mysql | tail -50` 看 SQL 错误

### Q3：访问 `http://IP` 浏览器一直转圈

- 检查云厂商**安全组** 80 端口开了没
- 检查服务器防火墙：`ufw status`，如果开了 ufw，执行 `ufw allow 80`

### Q4：内存爆了 OOM

```bash
docker compose ps        # 看是不是有容器 Exited (137)
free -h                  # 看可用内存
```

如果 swap 没生效（Swap 一行是 0B），回头做第 4.4 节。

### Q5：磁盘满了

```bash
df -h
docker system df
# 清理无用的镜像/容器
docker system prune -a --volumes
# ⚠️ 这会清掉所有未被使用的镜像，确认后再执行
```

### Q6：Swagger 看到了但接口报 500

看后端日志：

```bash
docker compose logs -f app | tail -100
```

大概率是缺表（schema.sql 没执行好）或者 SQL 语法不兼容。

---

## 13. 安全加固（强烈建议做）

上线后第一周内做掉这些：

### 13.1 禁止 root SSH 登录 + 改 SSH 端口（防爆破）

```bash
adduser mike                     # 创建普通用户
usermod -aG sudo mike            # 加 sudo 权限
su - mike
ssh-keygen                       # 生成密钥（一路回车）
# 把你本地的公钥（id_rsa.pub）内容贴到服务器 /home/mike/.ssh/authorized_keys
```

然后改 SSH 配置：

```bash
sudo vim /etc/ssh/sshd_config
# 改：
#   Port 22222                  # 改成非 22 的端口，记得安全组放行新端口
#   PermitRootLogin no
#   PasswordAuthentication no    # 只允许密钥登录
sudo systemctl restart sshd
```

⚠️ 改完别断开当前 SSH，**新开一个窗口用新端口登录测试**，能登录了再断开旧的。

### 13.2 装 fail2ban（防暴力破解）

```bash
apt install -y fail2ban
systemctl enable --now fail2ban
```

### 13.3 自动备份（每天凌晨 3 点）

```bash
mkdir -p /backup
cat > /opt/mall-backup.sh <<'EOF'
#!/bin/bash
cd /opt/ElectronicMallApi/deploy
source .env
docker exec em-mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} --all-databases > /backup/mysql-$(date +%F).sql
docker exec em-redis redis-cli -a ${REDIS_PASSWORD} SAVE 2>/dev/null
cp data/redis/dump.rdb /backup/redis-$(date +%F).rdb 2>/dev/null
find /backup -mtime +14 -delete
echo "[$(date)] backup done"
EOF

chmod +x /opt/mall-backup.sh
echo '0 3 * * * /opt/mall-backup.sh >> /var/log/mall-backup.log 2>&1' | crontab -
```

---

## 完成后的简历素材 ✨

上线后简历可以这么写：

> **电子商城 API 系统**（个人项目）
> - 技术栈：Spring Boot 3 / MyBatis-Plus / Redis / MySQL / Docker / Nginx
> - 独立完成 6 大业务模块（用户/商品/订单/秒杀/...）共 XX 个 REST API
> - 基于 Docker Compose 实现全栈容器化部署，运行于 2C2G 云服务器
> - 针对 2G 内存限制进行调优：JVM 堆 384M + G1GC、MySQL buffer pool 256M、4G swap，QPS 稳定 30+
> - 公网可访问：http://xxx.xxx.xxx.xxx/swagger-ui/index.html
> - 实现 Redis + Lua 脚本保证秒杀原子性、Redisson 分布式锁、JWT 鉴权等

**这比"我跑过 demo"硬核多了。**

---

## 还有问题？

部署中遇到任何问题，把下面信息一起发我：

```bash
docker compose ps
docker compose logs --tail=50 app
free -h
df -h
```

把这 4 条的输出贴出来，我帮你定位。
