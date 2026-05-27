# 社区互助平台 - 部署文档

## 一、环境要求

### 1.1 基础环境

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 推荐 OpenJDK 17 |
| Maven | 3.8+ | 构建工具 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存服务 |
| RabbitMQ | 3.12+ | 消息队列 |
| Elasticsearch | 8.x | 搜索引擎（可选） |

### 1.2 服务器配置建议

| 服务 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 网关 | 2核 | 4GB | 20GB |
| 用户服务 | 2核 | 4GB | 20GB |
| 订单服务 | 4核 | 8GB | 40GB |
| 支付服务 | 2核 | 4GB | 20GB |
| 消息服务 | 2核 | 4GB | 20GB |
| 积分服务 | 1核 | 2GB | 10GB |
| 活动服务 | 1核 | 2GB | 10GB |
| 管理后台 | 1核 | 2GB | 10GB |

## 二、部署步骤

### 2.1 数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p < sql/init.sql

# 2. 验证数据库
mysql -u root -p -e "USE community_platform; SHOW TABLES;"
```

### 2.2 配置修改

每个服务需要修改 `application.yml` 配置文件：

```yaml
# 示例：user-service/application.yml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

jwt:
  secret: your_jwt_secret_key_here_must_be_at_least_256_bits
  expiration: 86400000
```

### 2.3 构建项目

```bash
# 1. 进入项目目录
cd backend-java

# 2. 编译打包
mvn clean package -DskipTests

# 3. 验证构建结果
ls -la */target/*.jar
```

### 2.4 启动服务

```bash
# 按顺序启动服务
java -jar gateway/target/gateway-1.0.0.jar &
java -jar user-service/target/user-service-1.0.0.jar &
java -jar order-service/target/order-service-1.0.0.jar &
java -jar payment-service/target/payment-service-1.0.0.jar &
java -jar message-service/target/message-service-1.0.0.jar &
java -jar credit-service/target/credit-service-1.0.0.jar &
java -jar activity-service/target/activity-service-1.0.0.jar &
java -jar admin-service/target/admin-service-1.0.0.jar &
```

### 2.5 验证服务

```bash
# 检查服务端口
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# 测试登录接口
curl -X POST http://localhost:8080/api/v1/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"user123"}'
```

## 三、Docker部署

### 3.1 Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### 3.2 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: community_platform
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --requirepass your_redis_password

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin_password

  gateway:
    build: ./gateway
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis

  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

## 四、Nginx配置

```nginx
upstream gateway {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://gateway;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://gateway;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 五、监控与日志

### 5.1 日志配置

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.community: DEBUG
  file:
    name: logs/app.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 5.2 健康检查

```bash
# 检查所有服务状态
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```
