FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Instalamos la librería compartida
COPY commons-lib/ ./commons-lib/
RUN mvn -f commons-lib/pom.xml clean install -DskipTests

# Construimos el servicio
COPY services/inventory-service/ ./services/inventory-service/
RUN mvn -f services/inventory-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/inventory-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
