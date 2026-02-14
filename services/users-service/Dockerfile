FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Instalamos la librería compartida
COPY commons-lib/ ./commons-lib/
RUN mvn -f commons-lib/pom.xml clean install -DskipTests

# Construimos el servicio
COPY services/users-service/ ./services/users-service/
RUN mvn -f services/users-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/services/users-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
