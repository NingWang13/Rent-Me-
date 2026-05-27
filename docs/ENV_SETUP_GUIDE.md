# Java 21 和 Maven 环境配置指南

## 1. 安装 Java 21 (LTS)

### Windows 系统

#### 方法一：使用 Oracle JDK
1. 访问 Oracle JDK 下载页面：https://www.oracle.com/java/technologies/downloads/#java21
2. 下载 Windows x64 Installer (.msi)
3. 双击安装，按照向导完成安装
4. 安装程序会自动配置 JAVA_HOME 环境变量

#### 方法二：使用 OpenJDK（推荐）
1. 访问 Eclipse Temurin 下载页面：https://adoptium.net/temurin/releases/?version=21
2. 选择 Windows x64 版本，下载 .msi 安装包
3. 双击安装，按照向导完成安装

#### 验证安装
打开新的命令提示符或 PowerShell 窗口，执行：
```bash
java -version
```
预期输出：
```
openjdk version "21.0.x" ...
```

### 手动配置环境变量（如果安装程序未自动配置）

1. 找到 Java 安装路径（例如：`C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot\`）
2. 右键"此电脑" → "属性" → "高级系统设置" → "环境变量"
3. 新建系统变量：
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot`（根据实际路径修改）
4. 编辑 Path 变量，添加：`%JAVA_HOME%\bin`

---

## 2. 安装 Maven

### Windows 系统

1. 访问 Maven 下载页面：https://maven.apache.org/download.cgi
2. 下载最新版本的 Binary zip archive（例如：`apache-maven-3.9.x-bin.zip`）
3. 解压到合适的位置（例如：`C:\Program Files\Apache\maven\`）
4. 配置环境变量：
   - 新建系统变量：
     - 变量名：`MAVEN_HOME`
     - 变量值：`C:\Program Files\Apache\maven`（根据实际路径修改）
   - 编辑 Path 变量，添加：`%MAVEN_HOME%\bin`

#### 验证安装
打开新的命令提示符或 PowerShell 窗口，执行：
```bash
mvn -version
```
预期输出：
```
Apache Maven 3.9.x ...
Maven home: C:\Program Files\Apache\maven
Java version: 21.0.x ...
```

---

## 3. 配置 Maven 镜像（加速依赖下载）

编辑 Maven 配置文件 `conf/settings.xml`，在 `<mirrors>` 标签内添加阿里云镜像：

```xml
<mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

---

## 4. 编译后端项目

### 步骤 1：打开命令提示符或 PowerShell

导航到项目后端目录：
```bash
cd C:\Users\Administrator\Desktop\小程序\backend-java
```

### 步骤 2：执行编译命令

```bash
# 清理并编译所有模块，跳过测试
mvn clean compile -DskipTests
```

### 步骤 3：检查编译结果

- 如果编译成功，会显示：`BUILD SUCCESS`
- 如果编译失败，会显示错误信息，请根据错误信息修复问题

### 常见问题

#### 问题 1：依赖下载失败
**解决方案**：
1. 检查网络连接
2. 确认已配置阿里云镜像
3. 执行 `mvn clean compile -U -DskipTests` 强制更新快照依赖

#### 问题 2：Java 版本不匹配
**错误信息**：`class file version 65.0 ...`
**解决方案**：确认 JAVA_HOME 指向 Java 21，执行：
```bash
echo %JAVA_HOME%
java -version
```

#### 问题 3：内存不足
**解决方案**：设置 Maven 内存参数，在命令行执行：
```bash
set MAVEN_OPTS=-Xmx1024m -XX:MaxPermSize=512m
```
或者在环境变量中新建 `MAVEN_OPTS`，值为 `-Xmx1024m -XX:MaxPermSize=512m`

---

## 5. 编译各个服务模块

如果根目录编译成功，可以单独编译各个服务：

```bash
# 编译用户服务
cd user-service
mvn clean compile -DskipTests

# 编译支付服务
cd ../payment-service
mvn clean compile -DskipTests

# 编译订单服务
cd ../order-service
mvn clean compile -DskipTests
```

---

## 6. 导入 IDE 进行开发

### IntelliJ IDEA（推荐）

1. 打开 IntelliJ IDEA
2. File → Open → 选择 `backend-java` 目录
3. 等待 Maven 项目导入完成
4. 配置 Project SDK 为 Java 21：
   - File → Project Structure → Project → SDK → 选择 Java 21
   - File → Project Structure → Project → Language level → 21

### Eclipse

1. 打开 Eclipse
2. File → Import → Maven → Existing Maven Projects
3. 选择 `backend-java` 目录
4. 完成导入后，右键项目 → Build Path → Configure Build Path → Libraries → 选择 Java 21 JRE

---

## 7. 验证微信支付 SDK 依赖

编译成功后，验证微信支付 SDK 是否正确引入：

```bash
# 查看依赖树
cd payment-service
mvn dependency:tree | findstr wechat
```

预期输出应包含：
```
com.wechat.pay:wechatpay-java:jar:0.4.9:compile
```

---

## 8. 下一步

环境配置完成并编译成功后，可以：

1. 启动后端服务（需要配置数据库和 Redis）
2. 在微信开发者工具中测试前端页面
3. 进行接口联调测试

---

## 附录：快速安装命令（可选）

如果您使用 Chocolatey 包管理器（Windows）：

```bash
# 安装 Chocolatey（以管理员身份运行 PowerShell）
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# 安装 Java 21
choco install openjdk21 -y

# 安装 Maven
choco install maven -y
```

如果您使用 Scoop 包管理器（Windows）：

```bash
# 安装 Scoop（在 PowerShell 中执行）
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex

# 安装 Java 21
scoop install openjdk21

# 安装 Maven
scoop install maven
```
