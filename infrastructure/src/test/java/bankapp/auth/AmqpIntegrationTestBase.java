package bankapp.auth;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest
@ActiveProfiles("test")
public class AmqpIntegrationTestBase {

    @Container
    @ServiceConnection
    protected final static RabbitMQContainer rabbitmqContainer = new RabbitMQContainer("rabbitmq:4.1.3-management");

}
