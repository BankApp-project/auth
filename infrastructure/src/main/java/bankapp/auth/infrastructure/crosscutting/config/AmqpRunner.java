package bankapp.auth.infrastructure.crosscutting.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test & !test-postgres")
@Component
@RequiredArgsConstructor
public class AmqpRunner implements CommandLineRunner {

    private final AmqpAdmin amqpAdmin;
    private final TopicExchange notificationTopicExchange;

    @Override
    public void run(String... args) {
        amqpAdmin.declareExchange(notificationTopicExchange);
    }
}
