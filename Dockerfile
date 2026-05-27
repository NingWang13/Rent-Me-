FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY backend-java/pom.xml ./backend-java/
COPY backend-java/common/pom.xml ./backend-java/common/
COPY backend-java/user-service/pom.xml ./backend-java/user-service/
COPY backend-java/activity-service/pom.xml ./backend-java/activity-service/
COPY backend-java/admin-service/pom.xml ./backend-java/admin-service/
COPY backend-java/credit-service/pom.xml ./backend-java/credit-service/
COPY backend-java/service-service/pom.xml ./backend-java/service-service/
COPY backend-java/gateway/pom.xml ./backend-java/gateway/

RUN cd backend-java && mvn dependency:go-offline -DskipTests

COPY backend-java/ ./

RUN cd backend-java && mvn package -DskipTests -pl common,user-service,activity-service,admin-service,credit-service,service-service,gateway

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/backend-java/user-service/target/*.jar /app/services/user-service.jar
COPY --from=build /app/backend-java/activity-service/target/*.jar /app/services/activity-service.jar
COPY --from=build /app/backend-java/admin-service/target/*.jar /app/services/admin-service.jar
COPY --from=build /app/backend-java/credit-service/target/*.jar /app/services/credit-service.jar
COPY --from=build /app/backend-java/service-service/target/*.jar /app/services/service-service.jar
COPY --from=build /app/backend-java/gateway/target/*.jar /app/services/gateway.jar

EXPOSE 8080 8081 8082 8083 8084 8085

CMD ["java", "-jar", "/app/services/gateway.jar"]