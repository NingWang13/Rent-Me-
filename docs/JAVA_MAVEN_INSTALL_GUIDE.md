# Java 21 和 Maven 一键安装指南

## 方法一：使用 Chocolatey 一键安装（推荐）

### 步骤 1：安装 Chocolatey
以**管理员身份**打开 PowerShell，执行：
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor [System.Net.SecurityProtocolType]::Tls12
iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
```

### 步骤 2：安装 Java 21 和 Maven
```powershell
choco install openjdk21 maven -y
```

### 步骤 3：验证安装
重新打开 PowerShell，执行：
```powershell
java -version
mvn -version
```

---

## 方法二：手动安装（如果 Chocolatey 安装失败）

### 步骤 1：下载 Java 21
访问：https://adoptium.net/temurin/releases/?version=21
- 选择：Windows x64
- 下载：`.msi` 安装包
- 双击安装，按照向导完成

### 步骤 2：下载 Maven
访问：https://maven.apache.org/download.cgi
- 下载：`apache-maven-3.9.6-bin.zip`
- 解压到：`C:\Program Files\Apache\maven\`

### 步骤 3：配置环境变量
1. 右键"此电脑" → "属性" → "高级系统设置" → "环境变量"
2. 新建系统变量：
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot`（根据实际安装路径）
3. 新建系统变量：
   - 变量名：`MAVEN_HOME`
   - 变量值：`C:\Program Files\Apache\maven`
4. 编辑 `Path` 变量，添加：
   - `%JAVA_HOME%\bin`
   - `%MAVEN_HOME%\bin`

### 步骤 4：验证安装
重新打开命令提示符或 PowerShell，执行：
```bash
java -version
mvn -version
```

---

## 方法三：使用项目自带 Maven Wrapper（无需安装 Maven）

项目已包含 `mvnw.cmd`（Maven Wrapper），可以直接使用：

```bash
# 使用项目自带的 Maven 编译
cd C:\Users\Administrator\Desktop\小程序\backend-java
mvnw.cmd clean compile -DskipTests
```

这会**自动下载** Maven 到本地，无需手动安装。

---

## 快速验证脚本

创建文件 `check_env.bat`，内容如下：
```batch
@echo off
echo Checking Java...
java -version 2>&1 | findstr "version"
if errorlevel 1 (
    echo [ERROR] Java not found! Please install Java 21.
) else (
    echo [OK] Java is installed.
)

echo.
echo Checking Maven...
mvn -version 2>&1 | findstr "Maven"
if errorlevel 1 (
    echo [WARNING] Maven not found in PATH. You can use mvnw.cmd instead.
) else (
    echo [OK] Maven is installed.
)

echo.
echo Checking Java HOME...
if not defined JAVA_HOME (
    echo [WARNING] JAVA_HOME is not set.
) else (
    echo [OK] JAVA_HOME=%JAVA_HOME%
)

pause
```

运行此脚本快速检查环境。

---

## 下一步

环境配置完成后，执行：
```bash
cd C:\Users\Administrator\Desktop\小程序\backend-java
mvnw.cmd clean compile -DskipTests
```

如果编译成功，会显示：`BUILD SUCCESS`
