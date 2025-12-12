package com.company.project.template.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton configuration for TestContainers to ensure containers are started once
 * and shared across all integration tests.
 */
public class TestContainersConfig {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String MONGODB_IMAGE = "mongo:7.0";
    private static final String REDIS_IMAGE = "redis:7.4-alpine";
    private static final String RABBITMQ_IMAGE = "rabbitmq:3.13-management-alpine";

    private static volatile TestContainersConfig instance;

    public final PostgreSQLContainer<?> postgresContainer;
    public final MongoDBContainer mongoContainer;
    public final GenericContainer<?> redisContainer;
    public final RabbitMQContainer rabbitMQContainer;

    private TestContainersConfig() {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);

        mongoContainer = new MongoDBContainer(DockerImageName.parse(MONGODB_IMAGE))
                .withReuse(false);

        redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(6379)
                .withReuse(false);

        rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse(RABBITMQ_IMAGE))
                .withReuse(false);

        // Start all containers
        postgresContainer.start();
        mongoContainer.start();
        redisContainer.start();
        rabbitMQContainer.start();
    }

    public static TestContainersConfig getInstance() {
        if (instance == null) {
            synchronized (TestContainersConfig.class) {
                if (instance == null) {
                    instance = new TestContainersConfig();
                }
            }
        }
        return instance;
    }
}
