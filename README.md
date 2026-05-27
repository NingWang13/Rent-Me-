# Rent-Me &mdash; Three-Generation Mutual Help Community Platform

[![License MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![WeChat Mini Program](https://img.shields.io/badge/WeChat-Mini%20Program-07C160)](https://developers.weixin.qq.com/miniprogram/dev/framework/)

> **English** &nbsp;|&nbsp; [中文](#三代互助社区平台)

A community platform that connects seniors, young adults, and working professionals through a WeChat Mini Program. The platform enables cross-generational mutual help, skill sharing, and an credit-based incentive ecosystem.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [API Overview](#api-overview)
- [Deployment](#deployment)
- [Documentation](#documentation)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

| Module | Description | Status |
|--------|-------------|:------:|
| **User System** | WeChat login, profile management, JWT authentication, elder mode | ✅ |
| **Wish Wall** | Post wishes, browse community needs, match helpers | ✅ |
| **Order System** | Create / accept / complete orders, status tracking | ✅ |
| **Credit System** | Earn credits through mutual help, redeem rewards | ✅ |
| **Activity Center** | Community events, sign-ups, volunteer management | ✅ |
| **Admin Panel** | User management, content moderation, data dashboard | ✅ |
| **Messaging** | Real-time chat, group chat, system notifications | ✅ |
| **Accessibility** | Elder mode with large fonts, voice prompts, simplified UI | ✅ |
| **API Gateway** | Unified entry, routing, load balancing, rate limiting | ✅ |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend Framework** | Spring Boot 3.2.5, Spring Cloud 2023.0.3 |
| **ORM** | MyBatis-Plus 3.5.7 |
| **Database** | MySQL 8.0, Redis 7.2 |
| **Message Queue** | RabbitMQ |
| **API Gateway** | Spring Cloud Gateway |
| **Authentication** | JWT (JJWT 0.12.3) |
| **Frontend** | WeChat Mini Program (WXML / WXSS / JavaScript) |
| **Containerization** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |

---

## Project Structure

```
Rent-Me-/
├── miniapp/                        # WeChat Mini Program
│   ├── pages/                      #   Page modules
│   │   ├── index/                  #   Home page
│   │   ├── wish/                   #   Wish wall
│   │   ├── order/                  #   Orders
│   │   ├── message/                #   Messages
│   │   ├── user/                   #   User center
│   │   ├── chat/                   #   1-on-1 chat
│   │   ├── group-chat/             #   Group chat
│   │   ├── auth/                   #   Login & verification
│   │   ├── activity/               #   Community activities
│   │   ├── profile/                #   Feedback & accessibility
│   │   ├── settings/               #   Notification settings
│   │   └── search/                 #   Search
│   ├── components/                 #   Reusable UI components
│   ├── store/                      #   State management
│   ├── utils/                      #   Utility functions
│   ├── app.js / app.json / app.wxss
│   └── package.json
├── backend-java/                   # Java microservices
│   ├── common/                     #   Shared library
│   ├── user-service/               #   User & auth service
│   ├── activity-service/           #   Activity management
│   ├── admin-service/              #   Admin panel backend
│   ├── credit-service/             #   Credit & points
│   ├── service-service/            #   Service catalog
│   ├── gateway/                    #   API gateway
│   ├── sql/                        #   Database scripts
│   ├── docs/                       #   Backend documentation
│   ├── start-services.sh / .bat    #   Startup scripts
│   └── .env.example                #   Environment template
├── docs/                           # Project documentation
│   ├── DEPLOYMENT_GUIDE.md
│   ├── ENV_SETUP_GUIDE.md
│   ├── QUICK_START.md
│   └── ...
├── .github/workflows/              # CI/CD pipeline
│   └── ci-cd.yml
├── Dockerfile
├── .gitignore
├── LICENSE
└── README.md
```

---

## Quick Start

### Prerequisites

- JDK 21 (LTS)
- Maven 3.9+ (or use the bundled Maven Wrapper)
- MySQL 8.0+
- Redis 7.2+
- [WeChat DevTools](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)

### 1. Clone the Repository

```bash
git clone https://github.com/NingWang13/Rent-Me-.git
cd Rent-Me-
```

### 2. Initialize the Database

```bash
mysql -u root -p < backend-java/sql/init.sql
```

### 3. Configure Environment

Copy and edit the environment template:

```bash
cp backend-java/.env.example backend-java/.env
```

Fill in your database credentials, Redis password, and WeChat Mini Program credentials (AppID, AppSecret).

### 4. Start Backend Services

```bash
cd backend-java

# Linux / macOS
./start-services.sh

# Windows
start-services.bat
```

### 5. Launch the Mini Program

1. Open WeChat DevTools
2. Import project &rarr; select the `miniapp/` directory
3. Set your AppID in DevTools
4. Update `apiBaseUrl` in `miniapp/app.js` to point to your backend

For detailed testing instructions, see [docs/FRONTEND_TEST_GUIDE.md](docs/FRONTEND_TEST_GUIDE.md).

---

## API Overview

All APIs are accessed through the **Gateway (port 8080)**. Internal service ports:

| Service | Port | Key Endpoints |
|---------|:----:|---------------|
| Gateway | 8080 | Route forwarding, auth filter |
| User Service | 8081 | `/api/v1/auth/*`, `/api/v1/users/*` |
| Activity Service | 8082 | `/api/v1/activities/*` |
| Admin Service | 8083 | `/api/v1/admin/*` |
| Credit Service | 8084 | `/api/v1/credits/*` |
| Service Catalog | 8085 | `/api/v1/services/*` |

Full API specification: [backend-java/docs/API_SPECIFICATION.md](backend-java/docs/API_SPECIFICATION.md)

---

## Deployment

### Docker (Recommended)

```bash
docker build -t rent-me:latest .
docker run -d -p 8080:8080 --env-file backend-java/.env rent-me:latest
```

### Manual Deployment

```bash
cd backend-java
./mvnw clean package -DskipTests
java -jar gateway/target/gateway-*.jar
```

See [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) for production deployment details.

---

## Documentation

| Document | Description |
|----------|-------------|
| [QUICK_START.md](docs/QUICK_START.md) | Step-by-step getting started |
| [ENV_SETUP_GUIDE.md](docs/ENV_SETUP_GUIDE.md) | Environment configuration |
| [JAVA_MAVEN_INSTALL_GUIDE.md](docs/JAVA_MAVEN_INSTALL_GUIDE.md) | Java & Maven setup |
| [DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) | Production deployment |
| [DEVELOPMENT_CHECKLIST.md](docs/DEVELOPMENT_CHECKLIST.md) | Pre-launch checklist |
| [DEVELOPMENT_SUMMARY.md](docs/DEVELOPMENT_SUMMARY.md) | Development summary |
| [API_SPECIFICATION.md](backend-java/docs/API_SPECIFICATION.md) | API reference |
| [PERFORMANCE_OPTIMIZATION.md](backend-java/docs/PERFORMANCE_OPTIMIZATION.md) | Optimization notes |
| [TEST_REPORT.md](backend-java/docs/TEST_REPORT.md) | Test results |

---

## Roadmap

- [x] Core user system with WeChat login
- [x] Wish wall and order management
- [x] Credit and incentive system
- [x] Real-time chat and group chat
- [x] Elder mode & accessibility
- [x] Admin dashboard
- [ ] Docker Compose orchestration for all services
- [ ] Automated integration tests
- [ ] Performance monitoring dashboard

---

## Contributing

Contributions are welcome! Whether it's bug reports, feature suggestions, or pull requests &mdash; every bit of help makes this project better.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

Please read through existing documentation before submitting. For major changes, open an issue first to discuss what you'd like to change.

---

## License

This project is licensed under the MIT License &mdash; see the [LICENSE](LICENSE) file for details.

---

## Contact

- **Repository**: [github.com/NingWang13/Rent-Me-](https://github.com/NingWang13/Rent-Me-)
- **Issues**: [github.com/NingWang13/Rent-Me-/issues](https://github.com/NingWang13/Rent-Me-/issues)

---

<p align="center">
  <sub>Built with ❤️ for cross-generational community care</sub>
</p>

---

## 三代互助社区平台

> [English](#rent-me----three-generation-mutual-help-community-platform) &nbsp;|&nbsp; **中文**

### 项目简介

三代互助社区平台通过微信小程序连接老年人、年轻人和上班族，实现跨代互助、技能共享与积分激励的社区生态。

### 核心功能

- **互助心愿**：发布求助心愿，获得社区成员帮助
- **订单管理**：创建、接单、完成订单，全程可追踪
- **积分系统**：完成互助获得积分，积分可兑换奖励
- **消息通知**：订单通知、活动通知、系统通知，支持群聊
- **社区活动**：发布活动、在线报名、志愿者管理
- **长辈模式**：大字体、语音提示、简化操作界面
- **无障碍支持**：适配读屏软件，符合 WCAG 2.1 标准

### 技术架构

| 层级 | 技术栈 |
|------|--------|
| **后端框架** | Spring Boot 3.2.5, Spring Cloud 2023.0.3 |
| **ORM** | MyBatis-Plus 3.5.7 |
| **数据库** | MySQL 8.0, Redis 7.2 |
| **消息队列** | RabbitMQ |
| **认证授权** | JWT (JJWT 0.12.3) |
| **前端** | 微信小程序 (WXML / WXSS / JavaScript) |
| **部署** | Docker, GitHub Actions |

### 快速开始

```bash
# 克隆仓库
git clone https://github.com/NingWang13/Rent-Me-.git
cd Rent-Me-

# 初始化数据库
mysql -u root -p < backend-java/sql/init.sql

# 配置环境变量
cp backend-java/.env.example backend-java/.env

# 启动后端
cd backend-java && ./start-services.sh

# 用微信开发者工具打开 miniapp/ 目录即可预览
```

### 参与贡献

欢迎提交 Issue 和 Pull Request。详细贡献流程见上方 [Contributing](#contributing) 章节。

---

<p align="center">
  <sub>用技术连接代际关怀</sub>
</p>