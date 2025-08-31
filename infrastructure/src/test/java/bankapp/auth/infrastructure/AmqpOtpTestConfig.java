package bankapp.auth.infrastructure;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AmqpOtpTestConfig {

    @Value("${app.amqp.publisher.notifications.otp.routing-key}")
    private String routingKey;

    @Bean
    public Queue testQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding testBinding(
            TopicExchange testExchange,
            Queue testQueue
    ) {
        return BindingBuilder
                .bind(testQueue)
                .to(testExchange)
                .with(routingKey);
    }
}
