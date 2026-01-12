# Microservices Banking Platform

A production-ready microservices-based banking platform built with Spring Boot and Spring Cloud, demonstrating modern distributed systems architecture and best practices.

## Architecture Overview

This project implements a microservices architecture with the following components:

- **API Gateway**: Single entry point for all client requests with load balancing
- **Service Discovery**: Eureka server for dynamic service registration and discovery
- **Configuration Server**: Centralized configuration management
- **Business Services**: Users, Orders, Payments, and Inventory microservices

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

### Core Framework

- **Java 21**: Latest LTS version
- **Spring Boot 3.5.9**: Core application framework
- **Maven**: Dependency management and build tool

### Spring Cloud Components

- **Spring Cloud Config**: Centralized configuration management
- **Spring Cloud Gateway**: API Gateway with WebFlux
- **Netflix Eureka**: Service discovery and registration
- **OpenFeign**: Declarative REST client for inter-service communication

### Database & Persistence

- **PostgreSQL**: Relational database (one database per microservice)
- **Spring Data JPA**: Data access layer with Hibernate
- **Flyway**: Database migration and versioning

### Additional Libraries

- **Lombok**: Boilerplate code reduction
- **MapStruct**: Type-safe object mapping
- **Jakarta Validation**: Input validation
- **Spring Boot Actuator**: Production-ready monitoring and metrics
- **Commons-Lib**: Shared library for standardized exception handling and error responses

### Development Tools

- **Spring Boot DevTools**: Hot reload during development

## Prerequisites

Before starting, ensure you have the following installed:

- **Java Development Kit (JDK) 21**
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Git**

## Database Setup

Create the following PostgreSQL databases:

```sql
CREATE DATABASE users_db;
CREATE DATABASE orders_db;
CREATE DATABASE payments_db;
CREATE DATABASE inventory_db;
```

Default credentials (configurable in Config Server):

- Username: `postgres`
- Password: `postgres`
- Host: `localhost`
- Port: `5432`

### Database Migrations with Flyway

Each microservice uses Flyway for database version control and schema migrations. Migration scripts are located in:

```
services/{service-name}/src/main/resources/db/migration/
```

#### Migration File Naming Convention

Follow the Flyway naming pattern:

```
V{version}__{description}.sql
```

Examples:

- `V1__create_users_table.sql`
- `V2__add_user_indexes.sql`
- `V3__alter_users_add_status.sql`

#### Migration Execution

Flyway migrations run automatically when each service starts:

1. Flyway checks the `flyway_schema_history` table
2. Executes pending migrations in version order
3. Records successful migrations in the history table

#### Example Migration Structure

```
inventory-service/src/main/resources/db/migration/
├── V1__create_products_table.sql
├── V2__add_product_indexes.sql
└── V3__insert_initial_products.sql
```

Each service's migrations are independent and manage their own database schema.

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd microservices_banking
```

### 2. Build the Project

Build commons-lib first, then all modules:

```bash
mvn clean install -DskipTests
```

This command will:

- Install commons-lib in local Maven repository
- Compile all microservices
- Generate MapStruct implementations
- Package all applications

### 3. Service Startup Order

Start services in the following order to ensure proper initialization:

#### Step 1: Start Eureka Server

```bash
cd infra/eureka-server
mvn spring-boot:run
```

Wait until the service is fully started. Access Eureka Dashboard at: `http://localhost:8761`

#### Step 2: Start Config Server

```bash
cd infra/config-server
mvn spring-boot:run
```

Wait until the service is fully started and registered with Eureka (check logs for "Started ConfigServerApplication").

#### Step 3: Start Business Services

Start all business services (order doesn't matter):

```bash
# Terminal 1 - Users Service
cd services/users-service
mvn spring-boot:run

# Terminal 2 - Inventory Service
cd services/inventory-service
mvn spring-boot:run

# Terminal 3 - Payments Service
cd services/payments-service
mvn spring-boot:run

# Terminal 4 - Orders Service
cd services/orders-service
mvn spring-boot:run
```

#### Step 4: Start API Gateway

```bash
cd infra/api-gateway
mvn spring-boot:run
```

### 4. Verify Deployment

- **Eureka Dashboard**: `http://localhost:8761`
    - All services should be registered (Users, Orders, Payments, Inventory, Gateway, Config Server)

- **API Gateway Health**: `http://localhost:8080/actuator/health`
    - Should return `{"status":"UP"}`

## Service Ports

| Service           | Port | Description                        |
|-------------------|------|------------------------------------|
| API Gateway       | 8080 | Entry point for all API requests   |
| Users Service     | 8081 | User management                    |
| Orders Service    | 8082 | Order processing and orchestration |
| Payments Service  | 8083 | Payment processing                 |
| Inventory Service | 8084 | Product inventory management       |
| Eureka Server     | 8761 | Service discovery                  |
| Config Server     | 8888 | Configuration management           |

## API Endpoints

All requests go through the API Gateway at `http://localhost:8080`

### Users Service

```
GET    /api/users/{id}              - Get user by ID
GET    /api/users/username/{username} - Get user by username
POST   /api/users                   - Create new user
PUT    /api/users/{id}              - Update user
DELETE /api/users/{id}              - Delete user
```

### Inventory Service

```
GET    /api/inventory/products/{id}          - Get product by ID
GET    /api/inventory/products/sku/{sku}     - Get product by SKU
GET    /api/inventory/products               - Get all products
POST   /api/inventory/products               - Create product
PUT    /api/inventory/products/{id}          - Update product
DELETE /api/inventory/products/{id}          - Delete product
POST   /api/inventory/products/reserve       - Reserve stock
POST   /api/inventory/products/release       - Release stock
GET    /api/inventory/products/check-stock/{sku}/{quantity} - Check availability
```

### Payments Service

```
POST   /api/payments/process                    - Process payment
GET    /api/payments/{id}                       - Get payment by ID
GET    /api/payments/transaction/{transactionId} - Get payment by transaction ID
GET    /api/payments/order/{orderId}            - Get payments by order
GET    /api/payments/user/{userId}              - Get payments by user
POST   /api/payments/{id}/refund                - Refund payment
```

### Orders Service

```
POST   /api/orders                  - Create order (orchestrates all services)
GET    /api/orders/{id}             - Get order by ID
GET    /api/orders/user/{userId}    - Get orders by user
POST   /api/orders/{id}/cancel      - Cancel order
```

## Configuration

Configuration files are centralized in the Config Server at:

```
infra/config-server/src/main/resources/config/
```

Files:

- `api-gateway.yml` - Gateway routes and load balancing
- `users-service.yml` - Users service configuration
- `orders-service.yml` - Orders service configuration
- `payments-service.yml` - Payments service configuration
- `inventory-service.yml` - Inventory service configuration

### Key Configuration Points

#### Database Configuration

Each service has its own database connection configured in its respective YAML file.

#### Eureka Configuration

All services register with Eureka at `http://localhost:8761/eureka/`

#### Feign Client Configuration

- Connection timeout: 5000ms
- Read timeout: 5000ms
- Logger level: BASIC

#### API Gateway Routes

Routes follow the pattern:

```
/api/{service-name}/** → lb://{service-name}
```

StripPrefix removes `/api` before forwarding to the target service.

## Project Structure

```
microservices_banking/
├── commons-lib/             # Shared library for exception handling
│   └── src/main/java/com/msbanking/commons/exception/
│       ├── ErrorResponse.java       # Standard error response DTO
│       ├── ErrorCode.java          # Centralized error catalog
│       └── BusinessException.java  # Base business exception
├── infra/
│   ├── api-gateway/         # API Gateway (Spring Cloud Gateway)
│   ├── config-server/       # Config Server (Spring Cloud Config)
│   └── eureka-server/       # Service Discovery (Netflix Eureka)
├── services/
│   ├── users-service/       # User management microservice
│   ├── orders-service/      # Order processing microservice
│   ├── payments-service/    # Payment processing microservice
│   └── inventory-service/   # Inventory management microservice
└── pom.xml                  # Parent POM with dependency management
```

Each microservice follows a layered architecture:

```
service/
├── controller/    # REST API endpoints
├── service/       # Business logic
├── repository/    # Data access layer
├── entity/        # JPA entities
├── dto/           # Data Transfer Objects
│   ├── request/   # Request DTOs
│   ├── response/  # Response DTOs
├── mapper/        # MapStruct mappers
├── client/        # Feign clients (Orders service only)
├── exception/     # Exception handling
│   └── GlobalExceptionHandler.java
└── enums/         # Enumerations
```

## Development Workflow

### Running in Development Mode

Spring Boot DevTools is enabled. Changes to Java files will trigger automatic restart.

### Testing Endpoints

Example: Create a user and fetch via Gateway

```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "password": "password123"
  }'

# Fetch user
curl http://localhost:8080/api/users/1
```

### Monitoring Services

- **Eureka Dashboard**: `http://localhost:8761` - View all registered services
- **Actuator Health**: `http://localhost:{port}/actuator/health` - Check service health
- **Actuator Info**: `http://localhost:{port}/actuator/info` - Service information

## Commons Library

The project includes a **shared library** (`commons-lib`) that provides standardized components for exception handling and error responses across all microservices.

### Components

- **ErrorResponse**: Standardized error response DTO with consistent structure
- **ErrorCode**: Centralized catalog of error codes organized by domain (ORD-XXX, USR-XXX, INV-XXX, PAY-XXX, GEN-XXX)
- **BusinessException**: Base exception class for business logic errors

### Installation

The library is installed in the local Maven repository and included as a dependency in all microservices:

```bash
cd commons-lib
mvn clean install
```

## Best Practices Implemented

- **Clean Code**: Meaningful names, SOLID principles, separation of concerns
- **Layer Separation**: Controller → Service → Repository pattern
- **DTO Pattern**: Separate DTOs from entities to avoid exposing internal structure
- **Validation**: Jakarta Bean Validation on request DTOs
- **Standardized Error Handling**: Shared commons-lib for consistent error responses
- **Error Catalog**: Centralized error codes for better traceability and monitoring
- **Database Migration**: Flyway for version-controlled schema changes
- **Service Discovery**: Dynamic service registration, no hardcoded URLs
- **Load Balancing**: Client-side load balancing via Eureka
- **Centralized Configuration**: Single source of truth for all configurations
- **Idiomatic Code**: Following Java and Spring Boot conventions

