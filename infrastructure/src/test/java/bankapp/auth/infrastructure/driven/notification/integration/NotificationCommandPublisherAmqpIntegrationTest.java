package bankapp.auth.infrastructure.driven.notification.integration;

import bankapp.auth.infrastructure.AmqpOtpTestConfig;
import bankapp.auth.infrastructure.driven.notification.NotificationCommandPublisher;
import bankapp.auth.infrastructure.driven.notification.SendEmailNotificationCommand;
import bankapp.auth.infrastructure.utils.WithRabbitMQContainer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(AmqpOtpTestConfig.class)
@SpringBootTest
@ActiveProfiles("test")
public class NotificationCommandPublisherAmqpIntegrationTest implements WithRabbitMQContainer {

    @Autowired
    private NotificationCommandPublisher commandPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Queue testQueue;

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
