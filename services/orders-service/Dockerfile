FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Instalamos la librería compartida
COPY commons-lib/ ./commons-lib/
RUN mvn -f commons-lib/pom.xml clean install -DskipTests

# Construimos el servicio
COPY services/orders-service/ ./services/orders-service/
RUN mvn -f services/orders-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/orders-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
