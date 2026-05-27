# 三代互助社区平台 - 开发完成检查清单

## 第一阶段：环境配置（预计 1-2 天）

### 1.1 Java 21 安装
- [ ] 下载并安装 Java 21（推荐 Eclipse Temurin）
- [ ] 配置 JAVA_HOME 环境变量
- [ ] 验证安装：`java -version` 显示 "21.x.x"

### 1.2 Maven 安装（或使用 Maven Wrapper）
- [ ] 下载并安装 Maven 3.9.x
- [ ] 配置 MAVEN_HOME 环境变量
- [ ] 验证安装：`mvn -version` 显示 "Apache Maven 3.9.x"
- [ ] 或者直接使用项目中的 `mvnw.cmd`（无需安装 Maven）

### 1.3 数据库准备
- [ ] 安装 MySQL 8.0.x
- [ ] 启动 MySQL 服务
- [ ] 执行数据库初始化脚本：`source scripts/init_database_simple.sql`
- [ ] 验证数据库创建成功：`SHOW DATABASES;` 显示 `mutual_help`

### 1.4 Redis 准备
- [ ] 安装 Redis
- [ ] 启动 Redis 服务
- [ ] 验证 Redis 运行：`redis-cli ping` 返回 `PONG`

---

## 第二阶段：后端编译与启动（预计 1 天）

### 2.1 编译项目
- [ ] 打开命令提示符或 PowerShell
- [ ] 导航到项目目录：`cd C:\Users\Administrator\Desktop\小程序\backend-java`
- [ ] 执行编译：`mvnw.cmd clean compile -DskipTests`
- [ ] 验证编译成功：显示 `BUILD SUCCESS`

### 2.2 配置微信参数
- [ ] 在微信公众平台获取小程序 AppID 和 AppSecret
- [ ] 在微信支付商户平台获取商户号、API 密钥
- [ ] 下载商户 API 证书（apiclient_cert.pem 和 apiclient_key.pem）
- [ ] 将证书放到 `payment-service/src/main/resources/wechat/` 目录
- [ ] 修改 `application.yml` 或使用环境变量配置这些参数

### 2.3 启动后端服务
- [ ] 启动用户服务：`cd user-service && mvnw.cmd spring-boot:run`
- [ ] 启动支付服务：`cd payment-service && mvnw.cmd spring-boot:run`
- [ ] 启动订单服务：`cd order-service && mvnw.cmd spring-boot:run`
- [ ] 验证服务启动成功：访问 `http://localhost:8081/api/v1/users/health`（需要自行添加健康检查接口）

---

## 第三阶段：前端测试（预计 2-3 天）

### 3.1 导入小程序项目
- [ ] 下载并安装微信开发者工具
- [ ] 打开微信开发者工具，扫码登录
- [ ] 导入项目：选择 `C:\Users\Administrator\Desktop\小程序\miniapp`
- [ ] 配置 AppID：使用您的小程序 AppID

### 3.2 配置后端 API 地址
- [ ] 打开 `miniapp/app.js`
- [ ] 修改 `apiBaseUrl` 为您的后端服务地址（例如：`http://localhost:8080`）
- [ ] 如果使用真机调试，需要使用局域网 IP 地址

### 3.3 测试首页
- [ ] 编译并预览首页
- [ ] 测试搜索功能
- [ ] 测试分类切换
- [ ] 测试下拉刷新
- [ ] 检查控制台是否有错误

### 3.4 测试微信登录
- [ ] 点击"我的"进入个人中心
- [ ] 检查是否触发微信登录
- [ ] 验证登录成功后用户信息显示正常
- [ ] 检查 token 是否正确存储

### 3.5 测试心愿墙
- [ ] 切换心愿墙页面
- [ ] 测试搜索功能
- [ ] 测试分类筛选
- [ ] 测试下拉刷新和上拉加载
- [ ] 点击心愿查看详情

### 3.6 测试订单功能
- [ ] 切换订单页面
- [ ] 测试订单状态切换
- [ ] 测试订单创建（需要后端接口支持）
- [ ] 测试微信支付（需要真实微信支付配置）

### 3.7 测试消息功能
- [ ] 切换消息页面
- [ ] 测试消息类型切换
- [ ] 测试全部已读功能
- [ ] 测试清空消息功能

---

## 第四阶段：功能完善（预计 3-5 天）

### 4.1 添加 API 参数验证
- [x] ~~创建带验证注解的请求 DTO~~（已完成）
- [x] ~~更新控制器添加 @Valid 注解~~（已完成）
- [x] ~~创建全局异常处理器~~（已完成）

### 4.2 编写单元测试
- [x] ~~创建 UserServiceTest.java~~（已完成）
- [x] ~~创建 PaymentServiceTest.java~~（已完成）
- [ ] 完善所有服务的单元测试
- [ ] 执行测试：`mvnw.cmd test`
- [ ] 验证测试通过率 > 80%

### 4.3 改进日志规范
- [x] ~~在控制器中添加日志记录~~（已完成）
- [ ] 在所有服务层添加日志记录
- [ ] 配置日志输出格式和级别

### 4.4 安全加固
- [ ] 实现请求签名验证
- [ ] 添加敏感数据加密（密码、身份证号等）
- [ ] 配置 HTTPS
- [ ] 防止 SQL 注入和 XSS 攻击

---

## 第五阶段：性能优化（预计 2-3 天）

### 5.1 数据库优化
- [x] ~~在数据库脚本中添加索引~~（已完成）
- [ ] 分析慢查询日志，优化 SQL 语句
- [ ] 考虑分表策略（如果数据量大的话）

### 5.2 缓存策略
- [ ] 配置 Redis 缓存
- [ ] 缓存热点数据（用户信息、心愿列表等）
- [ ] 设置合理的缓存过期时间

### 5.3 前端性能优化
- [ ] 压缩图片资源
- [ ] 使用分包加载（subpackages）
- [ ] 优化 `setData` 调用频率
- [ ] 减少首屏加载时间

---

## 第六阶段：提交审核（预计 1-2 周）

### 6.1 准备审核材料
- [ ] 准备小程序截图（至少 5 张）
- [ ] 准备功能介绍视频（可选）
- [ ] 填写小程序基本信息（名称、简介、类目等）
- [ ] 准备隐私政策文档

### 6.2 提交审核
- [ ] 在微信公众平台提交审核
- [ ] 等待审核结果（通常 1-7 个工作日）
- [ ] 如果审核被拒绝，根据反馈修改并重新提交

### 6.3 发布上线
- [ ] 审核通过后，点击"发布"
- [ ] 配置服务器域名（在微信公众平台）
- [ ] 监控线上运行情况

---

## 已完成任务总结

### ✅ 数据库脚本更新
- [x] 在 `sys_user` 表中添加 `openid` 字段

### ✅ 微信支付 SDK 集成
- [x] 添加 `wechatpay-java` SDK 依赖
- [x] 重写 `WechatPayService.java` 使用官方 SDK
- [x] 实现统一下单、查询订单、关闭订单、申请退款
- [x] 实现支付回调和退款回调处理

### ✅ API 参数验证
- [x] 创建带验证注解的请求 DTO（WechatLoginRequest, CreateOrderRequest, RefundRequest）
- [x] 更新控制器添加 @Valid 注解
- [x] 创建全局异常处理器（GlobalExceptionHandler, BusinessException）

### ✅ 开发文档
- [x] 创建环境配置指南（ENV_SETUP_GUIDE.md, JAVA_MAVEN_INSTALL_GUIDE.md）
- [x] 创建前端测试指南（FRONTEND_TEST_GUIDE.md）
- [x] 创建数据库初始化脚本（init_database_simple.sql）
- [x] 创建配置模板（application-template.yml）
- [x] 创建启动脚本（start-services.sh, start-services.bat）
- [x] 创建 Maven Wrapper（mvnw, mvnw.cmd）

### ✅ 单元测试
- [x] 创建 UserServiceTest.java
- [x] 创建 PaymentServiceTest.java

---

## 预计完成时间

| 阶段 | 预计时间 | 状态 |
|------|---------|------|
| 第一阶段：环境配置 | 1-2 天 | ⏳ 待完成 |
| 第二阶段：后端编译与启动 | 1 天 | ⏳ 待完成 |
| 第三阶段：前端测试 | 2-3 天 | ⏳ 待完成 |
| 第四阶段：功能完善 | 3-5 天 | 🔄 进行中 |
| 第五阶段：性能优化 | 2-3 天 | ⏳ 待完成 |
| 第六阶段：提交审核 | 1-2 周 | ⏳ 待完成 |

**总预计时间：3-4 周**

---

## 下一步行动

1. **立即开始**：按照第一阶段指南配置 Java 21 和 Maven 环境
2. **参考文档**：
   - `JAVA_MAVEN_INSTALL_GUIDE.md` - Java 和 Maven 安装指南
   - `ENV_SETUP_GUIDE.md` - 环境配置详细指南
   - `FRONTEND_TEST_GUIDE.md` - 前端测试指南
3. **执行脚本**：
   - `mvnw.cmd clean compile -DskipTests` - 编译项目
   - `start-services.bat` - 启动所有后端服务
4. **测试功能**：按照第三阶段指南在微信开发者工具中测试所有页面

---

**最后更新**：2026-05-18
**更新人**：WorkBuddy AI Agent
