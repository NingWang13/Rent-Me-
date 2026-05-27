#!/bin/bash
# 启动所有后端服务（Linux/Mac）

echo "启动三代互助社区平台后端服务..."

# 检查 Java 是否安装
if ! command -v java &> /dev/null; then
    echo "错误：未找到 Java，请先安装 Java 21"
    echo "参考文档：JAVA_MAVEN_INSTALL_GUIDE.md"
    exit 1
fi

# 检查 Maven 是否安装（或使用 mvnw）
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
elif [ -f "./mvnw" ]; then
    MVN_CMD="./mvnw"
    chmod +x ./mvnw
else
    echo "错误：未找到 Maven，请先安装 Maven 或使用 mvnw"
    echo "参考文档：JAVA_MAVEN_INSTALL_GUIDE.md"
    exit 1
fi

echo "使用 Maven 命令：$MVN_CMD"

# 编译项目
echo "步骤 1：编译项目..."
$MVN_CMD clean compile -DskipTests

if [ $? -ne 0 ]; then
    echo "错误：项目编译失败"
    exit 1
fi

echo "编译成功！"

# 启动各个服务
echo "步骤 2：启动服务..."

# 启动用户服务
echo "启动用户服务 (user-service)..."
cd user-service
$MVN_CMD spring-boot:run -Dspring-boot.run.profiles=local &
USER_PID=$!
echo "用户服务已启动，PID: $USER_PID"
cd ..

sleep 5

# 启动支付服务
echo "启动支付服务 (payment-service)..."
cd payment-service
$MVN_CMD spring-boot:run -Dspring-boot.run.profiles=local &
PAYMENT_PID=$!
echo "支付服务已启动，PID: $PAYMENT_PID"
cd ..

sleep 5

# 启动订单服务
echo "启动订单服务 (order-service)..."
cd order-service
$MVN_CMD spring-boot:run -Dspring-boot.run.profiles=local &
ORDER_PID=$!
echo "订单服务已启动，PID: $ORDER_PID"
cd ..

echo ""
echo "所有服务已启动！"
echo "用户服务 PID: $USER_PID"
echo "支付服务 PID: $PAYMENT_PID"
echo "订单服务 PID: $ORDER_PID"
echo ""
echo "查看日志：tail -f user-service/logs/spring.log"
echo "停止服务：kill $USER_PID $PAYMENT_PID $ORDER_PID"
