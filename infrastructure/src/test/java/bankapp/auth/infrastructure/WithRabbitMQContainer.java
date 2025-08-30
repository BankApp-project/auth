package bankapp.auth.infrastructure;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface WithRabbitMQContainer {

    @Container
    @ServiceConnection
    RabbitMQContainer rabbitmqContainer = new RabbitMQContainer("rabbitmq:4.1.3-management");

}
