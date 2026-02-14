# Microservices Banking Platform

A production-ready microservices-based banking platform built with Spring Boot and Spring Cloud. This project follows a decentralized architecture where each microservice is a completely independent Maven project, orchestrated via Docker.

## Architecture Overview

The system implements a distributed architecture with the following components:

- **API Gateway**: Single entry point for all client requests with dynamic routing and load balancing.
- **Service Discovery**: Eureka server for service registration and discovery.
- **Configuration Server**: Centralized configuration management using Spring Cloud Config.
- **Business Services**: Users, Orders, Payments, and Inventory microservices.
- **Shared Library**: A standalone library (commons-lib) for standardized error handling.

### Architecture Diagram

```
Client
  |
  v
API Gateway (8080)
  |
  +-- Eureka Server (8761)
  |
  +-- Config Server (8888)
  |
  +-- Users Service (8081)
  |
  +-- Orders Service (8082) ---> Users Service
  |                         ---> Inventory Service
  |                         ---> Payments Service
  |
  +-- Payments Service (8083)
  |
  +-- Inventory Service (8084)
```

## Technology Stack

### Core Frameworks
- **Java 21**: LTS version.
- **Spring Boot 3.5.9**: Core application framework.
- **Spring Cloud 2025.0.1**: Distributed systems patterns.
- **Maven**: Build and dependency management.

### Infrastructure & Persistence
- **Docker & Docker Compose**: Containerization and orchestration.
- **PostgreSQL 15**: Relational database (Independent instance per service).
- **Flyway**: Database versioning and automated migrations.

### Communication & Tooling
- **OpenFeign**: Declarative REST clients.
- **MapStruct**: High-performance object mapping.
- **Lombok**: Boilerplate reduction.
- **Spring Boot Actuator**: Health monitoring and metrics.

## Prerequisites

- **Docker & Docker Compose**: Recommended for production-like environments.
- **Java Development Kit (JDK) 21**: For manual development.
- **Maven 3.9+**: For manual development.

## Deployment with Docker

The project uses Docker Compose to orchestrate services and their respective databases. Each business service has its own isolated PostgreSQL container.

### 1. Run Everything

To build and start all infrastructure and business services:

```bash
docker-compose up --build -d
```

### 2. Update a Specific Service

If you modify the code of a single microservice, you can rebuild and update it without affecting the rest of the system:

```bash
docker-compose up -d --build <service-name>
```

Example for Users Service:
```bash
docker-compose up -d --build users-service
```

### 3. Update Shared Library (commons-lib)

Since business services depend on `commons-lib`, if you modify the library, you must rebuild the services that consume it:

```bash
docker-compose up -d --build users-service inventory-service orders-service payments-service
```

### 4. Stopping the Environment

```bash
docker-compose down
```

## Independent Development

Each microservice is now an independent Maven project. There is no parent POM at the root level.

### Manual Setup for Development

If you wish to run services outside of Docker:

1. **Install Shared Library**: Navigate to `commons-lib` and install it in your local Maven repository.
   ```bash
   cd commons-lib
   mvn clean install
   ```

2. **Database Setup**: Ensure you have PostgreSQL instances running and create the following databases:
   - `users_db`
   - `orders_db`
   - `payments_db`
   - `inventory_db`

3. **Start Services**: Run each service independently using Maven.
   ```bash
   mvn spring-boot:run
   ```

## Service Ports & Infrastructure

| Service           | Port | Description                        |
|-------------------|------|------------------------------------|
| API Gateway       | 8080 | Entry point for all API requests   |
| Users Service     | 8081 | User management                    |
| Orders Service    | 8082 | Order processing and orchestration |
| Payments Service  | 8083 | Payment processing                 |
| Inventory Service | 8084 | Product inventory management       |
| Eureka Server     | 8761 | Service discovery dashboard        |
| Config Server     | 8888 | Centralized configuration          |

## Project Structure

```
microservices_banking/
├── commons-lib/             # Independent shared library
├── infra/
│   ├── api-gateway/         # Independent Edge service
│   ├── config-server/       # Independent Config service
│   └── eureka-server/       # Independent Discovery service
├── services/
│   ├── users-service/       # Independent User service
│   ├── orders-service/      # Independent Order service
│   ├── payments-service/    # Independent Payment service
│   └── inventory-service/   # Independent Inventory service
└── docker-compose.yml       # Infrastructure orchestration
```

## Best Practices Implemented

- **Microservice Independence**: No shared build logic or parent wrappers. Each service is autonomous.
- **Database per Service**: Isolated data persistence with dedicated PostgreSQL containers.
- **Automated Migrations**: Flyway manages schema changes independently for each service.
- **Layered Architecture**: Clear separation between Controller, Service, and Repository layers.
- **Standardized Errors**: Centralized error catalog and response structure via `commons-lib`.
- **Health Monitoring**: Production-grade health checks and metrics using Spring Actuator.
