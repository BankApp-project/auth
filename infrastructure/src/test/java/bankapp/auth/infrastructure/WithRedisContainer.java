package bankapp.auth.infrastructure;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface WithRedisContainer {

    int REDIS_PORT = 6379;

    @Container
    @ServiceConnection
    GenericContainer<?> redisContainer = new GenericContainer<>("redis:8.2.1-alpine")
            .withExposedPorts(REDIS_PORT);
}
