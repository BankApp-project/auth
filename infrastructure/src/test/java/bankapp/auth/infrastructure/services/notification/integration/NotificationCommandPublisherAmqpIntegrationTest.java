package bankapp.auth.infrastructure.services.notification.integration;

import bankapp.auth.infrastructure.WithRabbitMQContainer;
import bankapp.auth.infrastructure.services.notification.NotificationCommandPublisher;
import bankapp.auth.infrastructure.services.notification.SendEmailNotificationCommand;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationCommandPublisherAmqpIntegrationTest implements WithRabbitMQContainer {

    @Autowired
    private NotificationCommandPublisher commandPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Queue testQueue;

    @TestConfiguration
    public static class testConfig {

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

    @Test
    void shouldPublishSendEmailCommandSuccessfully() {
        var email = "test@bankapp.online";
        var subject = "We are testing sending OTPs!";
        var body = "Hello from there :-)";

        var command = new SendEmailNotificationCommand(
                email,
                subject,
                body
        );

        commandPublisher.publishSendEmailCommand(command);
        var receivedPayload = rabbitTemplate.receiveAndConvert(testQueue.getActualName(), 500L);

        assertThat(receivedPayload)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(command);
    }
}
