package bankapp.auth;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class RedisIntegrationTestBase {

    private static final int REDIS_PORT = 6379;

    @Container
    @ServiceConnection
    protected final static GenericContainer<?> redisContainer = new GenericContainer<>("redis:8.2.1-alpine")
            .withExposedPorts(REDIS_PORT);
}
