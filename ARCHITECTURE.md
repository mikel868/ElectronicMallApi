# ElectronicMallApi 架构文档

> ⚠️ **AI 客服子系统已临时下线**（适配 2C2G 小内存服务器）。代码与配置全部以注释形式保留，恢复时按下方清单逐项取消注释即可。
>
> ### 恢复 AI 客服的清单
>
> | # | 文件 | 操作 |
> |---|------|------|
> | 1 | `pom.xml` | 取消 `langchain4j-*` 与 `spring-boot-starter-webflux` 整段 XML 注释；删除 `maven-compiler-plugin` 中的 `<excludes>` 节 |
> | 2 | `src/main/java/com/rabbiter/em/user/service/UserService.java` | 取消 `dev.langchain4j.service.UserName` import 的注释 |
> | 3 | `src/main/resources/application.yml` | 取消 `dev.langchain4j:` 日志行的注释 |
> | 4 | `src/main/resources/application-dev.yml` | 取消 `langchain4j:` 配置块的注释 |
> | 5 | `src/main/resources/application-prod.yml` | 取消 `langchain4j:` 配置块和日志行的注释 |
> | 6 | `deploy/.env.example` | 取消 `LLM_*` 与 `LANGCHAIN4J_LOG_LEVEL` 的注释 |
> | 7 | `deploy/nginx/mall.conf` | 取消 `/chat` location 块的注释 |
> | 8 | 数据库 | `ai_order` 表已包含在 `schema.sql` 中，无需额外操作 |
>
> AI 源码本身未做任何改动，参见 `src/main/java/com/rabbiter/em/ai/` 与 `src/main/java/com/rabbiter/em/config/ConsultantConfig.java`。

---

> 本文档是项目的「地图」。读完后你应能回答：项目分几层？AI 客服子系统怎么扩展？怎么从开发环境切到生产环境？怎么换 LLM / 向量库 / 存储后端？

---

## 1. 设计原则

| 原则 | 在代码中的体现 |
|------|---------------|
| **领域驱动** | Java 包按业务域切分（`user`/`goods`/`order`/`seckill`/`content`/`chatbot`），不按技术分层切包 |
| **端口适配器（Hexagonal）** | AI / 存储 / LLM 等外部依赖全部抽象成 Port 接口，业务只依赖接口 |
| **配置即环境变量** | 所有 IP / 密码 / 密钥都通过 `${ENV}` 注入，区分 `dev` / `prod` profile |
| **代码 ≠ 部署** | 物理部署（单机 Docker Compose）与代码组织解耦，按业务域边界渐进拆分 |
| **不破坏既有功能** | 重构以「新增 Adapter + Port」方式做，老代码继续工作，迁移按业务节奏推进 |

---

## 2. 三个正交维度（不要混在一起看）

之前架构图乱，是因为一张图同时画了三件事。现在分开看：

### 维度①业务域（横向切分）

```
┌─────────────────────────────────────────────────────────────────┐
│ user 用户域 │ goods 商品域 │ order 订单域 │ seckill 营销域     │
├─────────────────────────────────────────────────────────────────┤
│ content 内容域 │ ⭐ chatbot AI 客服域                              │
└─────────────────────────────────────────────────────────────────┘
```

| 业务域 | Controller | 主要依赖 |
|--------|-----------|---------|
| user | `UserController` | MySQL, Redis, JWT |
| goods | `GoodController` `CategoryController` `IconController` `CarouselController` `SearchController` | MySQL, **Elasticsearch** |
| order | `OrderController` `CartController` `AddressController` `IncomeController` | MySQL, Redis |
| seckill | `SeckillVoucherController` | MySQL, **Redis + Lua + Redisson** |
| content | `AvatarController` `FileController` `RoleController` `DataSyncController` | MySQL, **StorageService** |
| **chatbot** | `ChatController` `AiOrderController` | **LangChain4j**, Redis（记忆/向量）, DashScope |

### 维度②技术分层（每个业务域内部纵向）

```
Controller  →  Service(领域逻辑)  →  Mapper / Repository
                                →  Port(外部依赖抽象)
```

### 维度③物理部署（独立维度，详见第 6 节）

```
Nginx :80/:443 → Spring Boot :9191 → MySQL/Redis/ES（同机容器）
                                    → DashScope（公网 API）
```

---

## 3. 整体架构图

```
                    ┌──────────────────────────────────────────┐
   用户/浏览器  ───▶ │  接入层 Nginx                            │
                    │   - 前端静态托管 (dist/)                 │
                    │   - /api 反代到 Spring Boot              │
                    │   - /avatar /file 静态映射               │
                    │   - /chat SSE 流式 (关闭 proxy_buffering)│
                    │   - 秒杀接口 limit_req 限流              │
                    │   - HTTPS 终止（域名接入后启用）          │
                    └──────────────────┬───────────────────────┘
                                       │
                    ┌──────────────────▼───────────────────────┐
                    │  应用层 Spring Boot (单进程)              │
                    │                                          │
                    │  ┌──────────┐ ┌──────────┐ ┌──────────┐ │
                    │  │ 用户域   │ │ 商品域   │ │ 订单域   │ │
                    │  └──────────┘ └──────────┘ └──────────┘ │
                    │  ┌──────────┐ ┌──────────┐ ┌──────────┐ │
                    │  │ 营销域   │ │ 内容域   │ │ ⭐chatbot │ │
                    │  └──────────┘ └──────────┘ └──────────┘ │
                    │                                          │
                    │  共享层: shared/ (JWT/CORS/Result/异常)  │
                    │  存储层: storage/ (Port + Adapter)       │
                    └──────────────────┬───────────────────────┘
                                       │
              ┌────────────────────────┼───────────────────────────┐
              │                        │                           │
        ┌─────▼─────┐          ┌───────▼──────┐         ┌──────────▼──────────┐
        │  MySQL 8  │          │   Redis 7    │         │  Elasticsearch 7.12 │
        │  主数据   │          │ 缓存/锁/记忆 │         │   商品全文搜索      │
        └───────────┘          │   /向量存储  │         └─────────────────────┘
                               └──────────────┘
                                       │
                               ┌───────▼────────┐
                               │  对象存储      │
                               │  Local → OSS   │ ← StorageService 抽象
                               └────────────────┘

外部依赖：DashScope (通义千问) ← 通过 LlmClient Port 抽象，可换 DeepSeek/OpenAI/自部署
```

> **Elasticsearch 开关**：ES 通过 `app.es.enabled` 控制。设为 `false` 时，`ElasticsearchConfig` / `ElasticsearchService` / `ElasticsearchSyncService` / `DataSyncService` 这 4 个 Bean 不会被创建，`SearchController` 与 `DataSyncController` 自动回退到 MySQL LIKE 查询，`GoodService` 的 CRUD 同步逻辑也会跳过。生产部署模板（`deploy/docker-compose.yml`）默认**不启动 ES 容器**，并通过环境变量 `APP_ES_ENABLED=false` 关闭。
>
> 重新启用 ES 的步骤：① 设置 `APP_ES_ENABLED=true`；② 取消 `docker-compose.yml` 中 `es:` 服务块和 `app.depends_on` / `app.environment` 中相关行的注释；③ 重启服务（无需改代码）。

---

## 4. 目录结构

```
ElectronicMallApi - idea1/
├── pom.xml
├── Dockerfile                          # ★ 后端镜像多阶段构建
├── dockerignore                        # ★ Docker 构建排除
├── ARCHITECTURE.md                     # ★ 本文档
│
├── src/main/java/com/rabbiter/em/
│   ├── ElectronicMallApplication.java
│   │
│   ├── shared/                         # (规划中) 公共层：Result/异常/CORS/JWT
│   ├── user/                           # (规划中) 用户域
│   ├── goods/                          # (规划中) 商品域
│   ├── order/                          # (规划中) 订单域
│   ├── seckill/                        # (规划中) 营销域
│   ├── content/                        # (规划中) 内容域
│   │
│   ├── chatbot/                        # ⭐ AI 客服子系统（已搭建骨架）
│   │   ├── port/                       #   Port 接口（业务只依赖这里）
│   │   │   ├── LlmClient.java          #     LLM 抽象
│   │   │   ├── ChatbotVectorStore.java #     向量库抽象
│   │   │   ├── ChatbotMemoryStore.java #     会话记忆抽象
│   │   │   └── ChatbotTool.java        #     工具抽象
│   │   ├── application/                #   Application 层（编排）
│   │   │   └── ChatSessionService.java
│   │   └── infrastructure/             #   Adapter 实现
│   │       ├── DashScopeLlmAdapter.java
│   │       └── RedisVectorStoreAdapter.java
│   │
│   ├── storage/                        # ⭐ 文件存储抽象（已搭建骨架）
│   │   ├── StorageService.java         #   Port
│   │   └── LocalStorageAdapter.java    #   Adapter (本地磁盘)
│   │
│   ├── controller/  service/  entity/  # 现有代码（按业务域渐进迁移）
│   ├── mapper/  dto/  config/  utils/  # 现有代码
│   ├── aiservice/ConsultantService.java # LangChain4j @AiService (chatbot 委托给此)
│   ├── tools/ReservationTools.java      # AI 工具实现 (chatbot 委托给此)
│   └── common/                          # Result/CacheClient/RedisDistributedLock
│
├── src/main/resources/
│   ├── application.yml                 # ★ 公共配置
│   ├── application-dev.yml             # ★ 开发环境（连虚拟机）
│   ├── application-prod.yml            # ★ 生产环境（环境变量强制注入）
│   ├── mapper/                         # MyBatis XML
│   ├── content/                        # RAG 知识库 PDF
│   ├── seckill.lua                     # 秒杀 Lua 脚本
│   └── system.txt                      # AI 系统提示词
│
├── deploy/                             # ⭐ 部署侧文件
│   ├── docker-compose.yml              #   全栈编排
│   ├── .env.example                    #   环境变量模板
│   ├── deploy.sh                       #   一键部署脚本
│   └── nginx/
│       ├── mall.conf                   #   Nginx 配置
│       └── certs/                      #   HTTPS 证书（域名接入后）
│
└── frontend/dist/                      # 前端构建产物（部署时上传）
```

> **说明**：标注 `(规划中)` 的目录是按业务域重组的**目标结构**，采用渐进式迁移，不影响现有功能运行。

---

## 5. AI 客服子系统（chatbot）详解

这是项目中**最容易扩展错的地方**。本架构用「端口适配器」把它彻底隔离。

### 5.1 子系统内部结构

```
chatbot/
├── port/                      端口（业务依赖这里）
│   ├── LlmClient              ← 同步 / 流式对话
│   ├── ChatbotVectorStore     ← RAG 检索
│   ├── ChatbotMemoryStore     ← 会话记忆
│   └── ChatbotTool            ← 工具抽象
│
├── application/               应用层（编排）
│   └── ChatSessionService
│
└── infrastructure/            适配器（具体厂商实现）
    ├── DashScopeLlmAdapter        委托给 LangChain4j ConsultantService
    └── RedisVectorStoreAdapter    委托给 RedisEmbeddingStore
```

### 5.2 数据流（一次 /chat 请求）

```
浏览器 SSE
   │
   ▼
ChatController
   │
   ▼ (现有代码委托)
ChatSessionService.streamChat(memoryId, message)
   │
   ▼
LlmClient (Port)
   │
   ▼ (Adapter)
DashScopeLlmAdapter → ConsultantService (@AiService by LangChain4j)
   │   │
   │   ├─→ ContentRetriever (RAG) → ChatbotVectorStore → RedisEmbeddingStore
   │   ├─→ ChatMemoryProvider     → ChatbotMemoryStore → RedisChatMemoryStore
   │   └─→ ReservationTools (@Tool) → 调用 goods/order 业务域 Service
   │
   ▼
Flux<String> → SSE → 浏览器
```

### 5.3 扩展点（按业务需要选用）

| 想做的事 | 改什么 |
|---------|-------|
| 换 LLM 厂商（如换 DeepSeek） | 新增 `DeepSeekLlmAdapter implements LlmClient` + 切 yml `LLM_BASE_URL` |
| 自部署 Qwen | 新增 `OllamaLlmAdapter`，调用本地 Ollama API |
| 向量库换 Milvus | 新增 `MilvusVectorStoreAdapter implements ChatbotVectorStore` |
| 加新工具（如「申请退款」） | 实现 `ChatbotTool`，在 `ReservationTools` 加 `@Tool` 方法 |
| 上传新客服文档 | 调 `ChatSessionService.ingestKnowledge(text, metadata)` |
| 接入工单系统 | 新增 `TicketTool`，工具里调外部 API |

### 5.4 切换 Adapter 的方式（无侵入）

`@ConditionalOnMissingBean(LlmClient.class)`：默认走 DashScope，**当容器里出现其他实现时自动让位**。

例如未来你写了一个 `DeepSeekLlmAdapter` 加上 `@Primary`，所有依赖 `LlmClient` 的业务自动切到 DeepSeek，**无需改一行业务代码**。

---

## 6. 物理部署

### 6.1 单机 Docker Compose（当前阶段推荐）

```
                阿里云/腾讯云 ECS (4核 8G+, Ubuntu 22.04)
                ┌──────────────────────────────────────┐
                │  Nginx :80                           │
   公网  ──▶    │   ├─ /            → frontend/dist    │
                │   ├─ /api/        → app:9191         │
                │   ├─ /chat (SSE)  → app:9191         │
                │   ├─ /avatar/     → data/uploads     │
                │   └─ /file/       → data/uploads     │
                │                                      │
                │  app (Spring Boot) :9191 (内网)      │
                │   ├─ mysql:3306   (内网)             │
                │   ├─ redis:6379   (内网)             │
                │   └─ es:9200      (内网)             │
                └──────────────────────────────────────┘
                                  │
                                  ▼
                       DashScope API (公网)
```

启动方式见 `deploy/README.md`。

### 6.2 渐进式扩容路径

| 阶段 | 触发条件 | 动作 |
|------|---------|------|
| **L0 单机** | 起步 | docker compose 全栈 1 台机器 |
| **L1 拆中间件** | 日活 > 1000 | MySQL/Redis/ES 各自独立服务器或托管服务 |
| **L2 加队列削峰** | 秒杀 QPS > 500 | 引入 RabbitMQ，秒杀请求先入队 |
| **L3 应用横向** | 单 app 扛不住 | N app + Nginx upstream + 共享文件改 OSS |
| **L4 拆微服务** | 团队 > 5 人 | 按业务域拆 user-service / goods-service / chatbot-service |
| **L5 K8s** | 实例 > 5 台 | 全部迁 K8s，HPA 自动扩缩 |

每个阶段都是上一阶段的**自然演进**，业务代码改动量极小（因为已经按业务域切分）。

---

## 7. 配置管理

### 7.1 三层配置文件

| 文件 | 作用 |
|------|------|
| `application.yml` | 所有环境共享的公共配置（端口、MyBatis、日志） |
| `application-dev.yml` | 开发环境，**带默认值**，连虚拟机中间件 |
| `application-prod.yml` | 生产环境，**强制环境变量注入**（无默认值，避免误用） |

通过 `SPRING_PROFILES_ACTIVE=dev|prod` 切换。

### 7.2 敏感配置清单（必须环境变量化）

| 变量 | 用途 |
|------|------|
| `MYSQL_PASSWORD` | 数据库密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `LLM_API_KEY` (兼容旧 `API-KEY`) | DashScope 密钥 |
| `JWT_SECRET` | JWT 签名密钥（生产至少 32 字符随机串） |
| `CORS_ALLOWED_ORIGINS` | 生产白名单，不要用 `*` |

### 7.3 当前环境的迁移说明

你之前的配置（连 `192.168.13.132`）现在被吸收到 `application-dev.yml` 作为默认值，**dev profile 下行为完全一致**：

```yaml
# application-dev.yml 片段
spring:
  data:
    redis:
      host: ${REDIS_HOST:192.168.13.132}    # ← 默认值就是你的虚拟机
      port: ${REDIS_PORT:6379}
  elasticsearch:
    cluster-nodes: ${ES_HOST:192.168.13.132}:${ES_PORT:9200}
```

直接运行（默认 profile=dev）行为不变；要切生产只需：
```bash
export SPRING_PROFILES_ACTIVE=prod
# 然后注入上面表格中的环境变量
```

---

## 8. 安全加固（上线前必做）

| # | 项 | 操作 |
|---|----|------|
| 1 | CORS 收紧 | `application-prod.yml` 中 `CORS_ALLOWED_ORIGINS=https://your-domain.com` |
| 2 | JWT 密钥 | `JWT_SECRET` 至少 32 位随机串，不要用 username 当密钥 |
| 3 | 中间件只绑本机 | docker-compose 中端口写 `"127.0.0.1:3306:3306"` |
| 4 | 安全组 | 阿里云控制台只放行 22/80/443 |
| 5 | 数据库账号分离 | 应用用 `mall_app` 账号（仅 DML），不用 root |
| 6 | 秒杀限流 | Nginx `limit_req` 已配置（10r/s burst=20） |
| 7 | 备份定时 | mysqldump + redis SAVE + ES snapshot，每日凌晨 cron |
| 8 | 日志脱敏 | logback 配置 mask 手机号/密码字段 |

---

## 9. 可观测性（建议接入）

| 维度 | 工具 | 用途 |
|------|------|------|
| 日志 | Loki + Promtail | 容器日志聚合 |
| 指标 | Prometheus + Grafana | JVM/Redis/MySQL/ES |
| 链路 | Micrometer Tracing + Zipkin | 秒杀场景定位瓶颈 |
| 告警 | Alertmanager | CPU/内存/响应时间异常 |

最小起步：Prometheus + Grafana + Node Exporter。

---

## 10. 备份与恢复

### 10.1 数据卷
`deploy/data/` 整个目录就是数据快照。换服务器只要 `rsync` 这一个目录。

### 10.2 定时备份脚本（建议加入 crontab）

```bash
# /opt/mall/backup.sh - 每天 3:00 执行
docker exec em-mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} --all-databases > /backup/mysql-$(date +%F).sql
docker exec em-redis redis-cli -a ${REDIS_PASSWORD} SAVE
cp -r /opt/mall/deploy/data/redis/dump.rdb /backup/redis-$(date +%F).rdb
# ES 通过 snapshot API 备份到共享目录
find /backup -mtime +14 -delete   # 保留 14 天
```

crontab:
```
0 3 * * * /opt/mall/backup.sh >> /var/log/mall-backup.log 2>&1
```

---

## 11. 迁移路径（如何把现有代码搬到新结构）

为避免一次性大重构破坏可用功能，采用**渐进迁移**：

### Sprint 1（已完成，本次重构）
- ✅ 搭建 `chatbot/` 端口与适配器骨架
- ✅ 搭建 `storage/` 端口与适配器骨架
- ✅ 配置文件 profile 化（dev/prod）
- ✅ 部署侧文件（Dockerfile / compose / nginx）

### Sprint 2（建议下一步）
- [ ] 把 `AvatarService` `FileService` 内部改用 `StorageService` Port
- [ ] `ChatController` 改为依赖 `ChatSessionService`（而非直接调 ConsultantService）
- [ ] JWT 密钥独立化（不再用 username 当 HMAC key）

### Sprint 3（中期）
- [ ] Controller/Service 按业务域重新归包
  - `controller/` `service/` 等老包暂留为兼容
  - 新代码按域归入 `user/` `goods/` `order/` `seckill/` `content/`
  - 配合 `@ComponentScan` 多路径扫描，过渡期可同时存在

### Sprint 4（按需）
- [ ] 接入 OSS（实现 `OssStorageAdapter`）
- [ ] 接入消息队列（秒杀削峰）
- [ ] 监控告警体系

---

## 12. 一句话总结

> **代码按业务域分（user/goods/order/seckill/content/chatbot），AI 客服用 Port-Adapter 隔离 LLM/向量库/记忆/工具；文件存储用 StorageService 抽象；配置 profile 化（dev/prod），所有密钥环境变量注入；物理部署 Docker Compose 单机起步，按 L0→L5 路径渐进扩展。**

---

## 13. 关键文件索引

| 我想看 / 改... | 文件 |
|---------------|------|
| LLM 抽象 | `chatbot/port/LlmClient.java` |
| DashScope 适配 | `chatbot/infrastructure/DashScopeLlmAdapter.java` |
| 向量库抽象 | `chatbot/port/ChatbotVectorStore.java` |
| Redis 向量适配 | `chatbot/infrastructure/RedisVectorStoreAdapter.java` |
| 文件存储抽象 | `storage/StorageService.java` |
| 本地文件适配 | `storage/LocalStorageAdapter.java` |
| 公共配置 | `src/main/resources/application.yml` |
| 开发配置 | `src/main/resources/application-dev.yml` |
| 生产配置 | `src/main/resources/application-prod.yml` |
| 全栈编排 | `deploy/docker-compose.yml` |
| Nginx 配置 | `deploy/nginx/mall.conf` |
| 环境变量模板 | `deploy/.env.example` |
| 一键部署 | `deploy/deploy.sh` |
| 后端镜像 | `Dockerfile` |
