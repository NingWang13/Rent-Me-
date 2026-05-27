# 三代互助社区平台 - 部署指南

## 1. 环境准备

### 1.1 生产服务器要求
- **操作系统**：Linux (Ubuntu 20.04 / CentOS 7+)
- **内存**：至少 4GB RAM
- **存储**：至少 20GB 可用空间
- **网络**：固定 IP 地址，开放端口 80/443/8080

### 1.2 安装 Docker 和 Docker Compose
```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 安装 Docker Compose
sudo apt-get install docker-compose-plugin

# 验证安装
docker --version
docker compose version
```

---

## 2. 配置生产环境

### 2.1 创建生产配置文件
创建 `application-prod.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/mutual_help?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  data:
    redis:
      host: redis
      port: 6379
      password: ${REDIS_PASSWORD}
      database: 0

wechat:
  app-id: ${WECHAT_APPID}
  app-secret: ${WECHAT_APPSECRET}
  pay:
    appid: ${WECHAT_PAY_APPID}
    mchid: ${WECHAT_PAY_MCHID}
    api-key: ${WECHAT_PAY_API_KEY}
    notify-url: https://yourdomain.com/api/v1/wechat-pay/callback

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    root: WARN
    com.community: INFO
```

### 2.2 创建环境变量文件
创建 `.env` 文件（**不要提交到 Git**）：
```
DB_USERNAME=prod_user
DB_PASSWORD=your_strong_password
REDIS_PASSWORD=your_redis_password
WECHAT_APPID=your_wechat_appid
WECHAT_APPSECRET=your_wechat_appsecret
WECHAT_PAY_APPID=your_wechat_pay_appid
WECHAT_PAY_MCHID=your_merchant_id
WECHAT_PAY_API_KEY=your_api_key
JWT_SECRET=your_jwt_secret_at_least_32_chars
```

---

## 3. 部署步骤

### 3.1 克隆项目到服务器
```bash
git clone https://github.com/your-repo/mutual-help-platform.git
cd mutual-help-platform
```

### 3.2 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件，填入实际的生产配置
nano .env
```

### 3.3 构建并启动服务
```bash
# 构建 Docker 镜像
docker compose build

# 启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 3.4 初始化数据库
```bash
# 进入 MySQL 容器
docker exec -it mutual-help-mysql mysql -u root -p

# 在 MySQL 中执行
source /docker-entrypoint-initdb.d/init.sql
```

---

## 4. 配置反向代理（Nginx）

### 4.1 安装 Nginx
```bash
sudo apt-get install nginx
```

### 4.2 配置 Nginx
创建 `/etc/nginx/sites-available/mutual-help`：
```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name yourdomain.com;

    # SSL 证书
    ssl_certificate /path/to/your/cert.pem;
    ssl_certificate_key /path/to/your/key.pem;

    # 反向代理到网关
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源（如果需要）
    location /static/ {
        alias /path/to/static/files/;
    }
}
```

### 4.3 启用配置
```bash
sudo ln -s /etc/nginx/sites-available/mutual-help /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## 5. 配置 HTTPS（使用 Let's Encrypt）

```bash
# 安装 Certbot
sudo apt-get install certbot python3-certbot-nginx

# 获取 SSL 证书
sudo certbot --nginx -d yourdomain.com

# 自动续期
sudo certbot renew --dry-run
```

---

## 6. 微信小程序配置

### 6.1 配置服务器域名
1. 登录微信公众平台
2. 进入"开发" → "开发管理" → "开发设置"
3. 添加服务器域名：
   - request 合法域名：`https://yourdomain.com`
   - socket 合法域名：（如果需要 WebSocket）
   - uploadFile 合法域名：（如果需要上传文件）
   - downloadFile 合法域名：（如果需要下载文件）

### 6.2 配置微信支付
1. 登录微信支付商户平台
2. 进入"产品中心" → "开发配置"
3. 配置支付授权目录：`https://yourdomain.com/api/v1/wechat-pay/`
4. 配置 H5 支付域名（如果需要）

---

## 7. 监控和维护

### 7.1 查看日志
```bash
# 查看所有服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f user-service
```

### 7.2 备份数据库
```bash
# 创建备份脚本 backup.sh
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec mutual-help-mysql mysqldump -u root -p$DB_PASSWORD mutual_help > backup_$DATE.sql
gzip backup_$DATE.sql

# 定时备份（每天凌晨 2 点）
crontab -e
0 2 * * * /path/to/backup.sh
```

### 7.3 更新部署
```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动
docker compose down
docker compose up -d --build
```

---

## 8. 故障排除

### 8.1 服务无法启动
```bash
# 查看容器日志
docker logs <container_name>

# 检查端口占用
sudo netstat -tulpn | grep 8080
```

### 8.2 数据库连接失败
```bash
# 检查 MySQL 容器状态
docker ps | grep mysql

# 检查网络连接
docker network inspect mutual-help-network
```

### 8.3 微信支付回调失败
- 确认 `notify-url` 配置正确
- 确认服务器防火墙已开放 443 端口
- 确认 Nginx 配置正确

---

## 9. 性能优化建议

1. **数据库优化**：
   - 为常用查询字段添加索引
   - 配置 MySQL 查询缓存
   - 定期清理日志表

2. **Redis 缓存**：
   - 缓存热点数据（用户信息、心愿列表）
   - 设置合理的缓存过期时间

3. **JVM 调优**：
   - 设置合理的堆内存大小：`-Xms512m -Xmx1024m`
   - 使用 G1 垃圾收集器：`-XX:+UseG1GC`

4. **Nginx 优化**：
   - 启用 Gzip 压缩
   - 配置静态资源缓存

---

## 10. 安全建议

1. **使用强密码**：数据库、Redis、JWT 密钥都使用强密码
2. **配置防火墙**：只开放必要端口（80, 443, SSH）
3. **定期更新系统**：`sudo apt-get update && sudo apt-get upgrade`
4. **禁用 root 远程登录**：修改 SSH 配置
5. **配置 Fail2ban**：防止暴力破解
6. **定期备份**：数据库和文件定期备份

---

**最后更新**：2026-05-18
**维护者**：WorkBuddy AI Agent
