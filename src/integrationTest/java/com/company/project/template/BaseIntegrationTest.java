package com.company.project.template;

import com.company.project.template.config.IntegrationTestConfig;
import com.company.project.template.config.TestContainersConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base integration test class that provides TestContainers setup for all infrastructure dependencies.
 * Containers are started once and shared across all integration tests for performance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
public abstract class BaseIntegrationTest {

    private static final TestContainersConfig containers = TestContainersConfig.getInstance();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", containers.postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", containers.postgresContainer::getUsername);
        registry.add("spring.datasource.password", containers.postgresContainer::getPassword);

        // MongoDB properties
        registry.add("spring.data.mongodb.uri", containers.mongoContainer::getReplicaSetUrl);

        // Redis properties
        registry.add("spring.data.redis.host", containers.redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> containers.redisContainer.getMappedPort(6379).toString());

        // RabbitMQ properties
        registry.add("spring.rabbitmq.host", containers.rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", containers.rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", containers.rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", containers.rabbitMQContainer::getAdminPassword);

        // Disable OpenTelemetry for tests
        registry.add("management.tracing.enabled", () -> "false");
        registry.add("management.otlp.tracing.endpoint", () -> "");
    }
}
