# =========================================================
#  Spring Boot 多阶段构建镜像
#  - 构建阶段：maven 编译
#  - 运行阶段：精简 JRE 镜像
# =========================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只拷 pom.xml 加速依赖缓存
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# -Dmaven.test.skip=true 同时跳过测试编译和执行
# （src/test 下有历史遗留的死代码测试，不参与生产构建）
RUN mvn clean package -Dmaven.test.skip=true -B

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 tzdata 设置时区
RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

COPY --from=builder /build/target/*.jar app.jar

# 健康检查（需要项目 actuator，可后续添加；先用进程存活）
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD curl -f http://localhost:9191/actuator/health || exit 1

EXPOSE 9191

# JVM 参数可通过 JAVA_OPTS 注入（如 -Xmx512m）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
