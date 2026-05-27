@echo off
REM 启动所有后端服务（Windows）

echo 启动三代互助社区平台后端服务...

REM 检查 Java 是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误：未找到 Java，请先安装 Java 21
    echo 参考文档：JAVA_MAVEN_INSTALL_GUIDE.md
    pause
    exit /b 1
)

REM 检查 Maven 是否安装（或使用 mvnw.cmd）
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set MVN_CMD=mvn
) else if exist "mvnw.cmd" (
    set MVN_CMD=mvnw.cmd
) else (
    echo 错误：未找到 Maven，请先安装 Maven 或使用 mvnw.cmd
    echo 参考文档：JAVA_MAVEN_INSTALL_GUIDE.md
    pause
    exit /b 1
)

echo 使用 Maven 命令：%MVN_CMD%

REM 编译项目
echo 步骤 1：编译项目...
%MVN_CMD% clean compile -DskipTests

if errorlevel 1 (
    echo 错误：项目编译失败
    pause
    exit /b 1
)

echo 编译成功！

REM 启动各个服务
echo 步骤 2：启动服务...

REM 启动用户服务
echo 启动用户服务 (user-service)...
cd user-service
start "User Service" cmd /c "%MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local"
cd ..

timeout /t 5 > nul

REM 启动支付服务
echo 启动支付服务 (payment-service)...
cd payment-service
start "Payment Service" cmd /c "%MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local"
cd ..

timeout /t 5 > nul

REM 启动订单服务
echo 启动订单服务 (order-service)...
cd order-service
start "Order Service" cmd /c "%MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local"
cd ..

echo.
echo 所有服务已启动！
echo 用户服务：http://localhost:8081
echo 支付服务：http://localhost:8083
echo 订单服务：http://localhost:8082
echo.
echo 查看日志：type user-service\logs\spring.log
echo 停止服务：关闭对应的命令行窗口
pause
