# Spring Boot Microservice Template

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![JDK](https://img.shields.io/badge/JDK-21-blue)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-latest-yellowgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-yellow)
![MongoDB](https://img.shields.io/badge/MongoDB-5.0.1-green)
![Redis](https://img.shields.io/badge/Redis-3.3.3-red)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-5.21.0-orange)
![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-1.37.0-purple)
![JUnit 5](https://img.shields.io/badge/JUnit%205-5.10.3-yellowgreen)
![ArchUnit](https://img.shields.io/badge/ArchUnit-1.2.1-cyan)
![GitHub](https://img.shields.io/github/license/isopropylcyanide/Jwt-Spring-Security-JPA?color=blue)

This is a template Spring Boot microservice implemented using **Spring Boot 3.3** and **Java 21**. It demonstrates the integration of various technologies and best practices for building microservices. Additionally, it showcases the usage of the [Common Library](https://github.com/mmushfiq/springboot-microservice-common-lib).

`{PN}` - _project name or project name abbreviation_

**Note:** This repository does not represent real-world business scenarios; the examples are solely intended to demonstrate the usage of various technologies.

## Technologies and Tools Used

This template integrates multiple tools and frameworks, including:

- **Docker Compose** - For containerized dependencies.
- **PostgreSQL** - Relational database.
- **Liquibase** - Database migration tool for PostgreSQL.
- **MongoDB** - NoSQL database.
- **Mongock** - Database migration tool for MongoDB.
- **Redis** - In-memory caching system.
- **RabbitMQ** - Message broker for event-driven communication.
- **ShedLock** - Ensures scheduled tasks run in distributed environments.
- **Logstash/Logback** - Centralized logging.
- **Checkstyle** - Code style enforcement.
- **OpenFeign** - Declarative REST client.
- **OpenTelemetry** - Observability and distributed tracing.
- **JUnit 5** - Unit testing framework.
- **ArchUnit** - Architecture rule validation.
- **TestContainers** - Integration testing with real infrastructure dependencies.
- **WireMock** - Mocking external HTTP services for testing.
- **Awaitility** - Testing asynchronous operations.

## Running the Application Locally

### Prerequisites

- **Java 21**
- **Gradle**
- **Docker & Docker Compose**

### Start Dependencies

Before running the application, start required dependencies using Docker Compose:

```sh
docker-compose up
```

This will create **PostgreSQL, MongoDB, Redis, RabbitMQ, and Jaeger** containers.

### Setting Up the Common Library Dependency

The **springboot-microservice-common-lib** dependency is not yet available in a central repository. To use it, you need to pull it from the GitHub package repository, which requires a **GPR_TOKEN**. Since this token is private and user-specific, it cannot be shared in this repository.

To include the dependency locally, follow these steps:

1. Clone the `springboot-microservice-common-lib` repository:
   ```sh
   git clone https://github.com/mmushfiq/springboot-microservice-common-lib.git
   ```
2. Navigate to the cloned directory:
   ```sh
   cd springboot-microservice-common-lib
   ```
3. Publish the library to your local Maven repository using Gradle:
   ```sh
   ./gradlew publishToMavenLocal
   ```

After completing these steps, the dependency will be available in your local Maven repository and can be used by this microservice.

### Running the Application

Run the application with the `local` profile. You can do this via IntelliJ IDEA run configurations or using the Gradle command:

```sh
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API Documentation

Swagger UI is available at:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Testing

This project includes comprehensive testing at multiple levels:

### Unit Tests

Located in `src/test/java`, unit tests use Mockito for mocking dependencies and test individual components in isolation.

Run unit tests:
```sh
./gradlew test
```

### Integration Tests

Located in `src/integrationTest/java`, integration tests use **TestContainers** to spin up real infrastructure dependencies (PostgreSQL, MongoDB, Redis, RabbitMQ) in Docker containers. These tests verify the full integration stack including:

- **Controller Integration Tests** - Test HTTP endpoints with real database interactions
- **Message Consumer Tests** - Test RabbitMQ message consumption and MongoDB persistence
- **External Service Tests** - Test OpenFeign clients with WireMock mocking
- **End-to-End Tests** - Test complete business flows across all infrastructure components

#### Running Integration Tests

**Prerequisites:**
- Docker must be running on your machine
- TestContainers will automatically download and start the required Docker images

Run integration tests:
```sh
./gradlew integrationTest
```

Run all tests (unit + integration):
```sh
./gradlew check
```

#### Integration Test Infrastructure

Integration tests use the following TestContainers:
- **PostgreSQL 16** - For account data persistence with Liquibase migrations
- **MongoDB 7.0** - For payment log storage with document-based data
- **Redis 7.4** - For payment caching with TTL
- **RabbitMQ 3.13** - For event-driven message processing

All containers are configured with `reuse=true` for faster test execution across multiple test classes.

#### Test Organization

```
src/integrationTest/
├── java/
│   └── com/company/project/template/
│       ├── BaseIntegrationTest.java                    # Base class with TestContainers setup
│       ├── controller/
│       │   ├── AccountControllerIntegrationTest.java   # Account CRUD with PostgreSQL
│       │   └── PaymentControllerIntegrationTest.java   # Payment creation/execution with Redis
│       ├── messaging/
│       │   └── MessageConsumerIntegrationTest.java     # RabbitMQ consumer with MongoDB
│       ├── client/
│       │   └── NotificationClientIntegrationTest.java  # OpenFeign client with WireMock
│       └── EndToEndPaymentFlowIntegrationTest.java     # Complete payment flow
└── resources/
    └── application.yaml                                 # Test-specific configuration
```

#### Key Integration Test Features

1. **HTTP Testing** - Uses `TestRestTemplate` to test REST controllers with full Spring context
2. **Database Testing** - Verifies Liquibase migrations and data persistence
3. **Cache Testing** - Tests Redis caching with TTL and serialization
4. **Message Queue Testing** - Validates RabbitMQ message publishing, consumption, and retry logic
5. **External Service Mocking** - Uses WireMock to simulate external notification service
6. **Asynchronous Testing** - Uses Awaitility for testing async message consumption

### Architecture Tests

ArchUnit tests validate architectural rules and layering principles:
```sh
./gradlew test --tests *ArchitectureTest
```

## Additional Tools

- **RabbitMQ UI** - Monitor queues:
  [http://localhost:15672](http://localhost:15672)
- **Jaeger UI** - View tracing data:
  [http://localhost:16686](http://localhost:16686)

## License

This project is licensed under the Apache-2.0 License.

