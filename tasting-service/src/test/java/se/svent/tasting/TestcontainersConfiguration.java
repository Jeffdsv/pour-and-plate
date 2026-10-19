package se.svent.tasting;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

// starts a fresh postgres in docker for tests and connects spring to it
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // @ServiceConnection makes spring use this database instead of the one in application.yaml
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        // same postgres version as docker compose, so tests match real life
        return new PostgreSQLContainer("postgres:18");
    }
}