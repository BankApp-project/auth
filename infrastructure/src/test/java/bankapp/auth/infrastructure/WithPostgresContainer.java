package bankapp.auth.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface WithPostgresContainer {

    @Container
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_auth")
            .withUsername("test")
            .withPassword("test");

    @BeforeEach
    default void setUp() {
        Flyway flyway = Flyway.configure().dataSource(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        ).load();
        flyway.migrate();
    }
}