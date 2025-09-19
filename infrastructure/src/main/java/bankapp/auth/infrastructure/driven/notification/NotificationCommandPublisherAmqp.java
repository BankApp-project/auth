package bankapp.auth.infrastructure.driven.notification;

import bankapp.payload.notification.email.otp.EmailNotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationCommandPublisherAmqp implements NotificationCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.amqp.publisher.notifications.otp.routing-key}")
    private String routingKey;

    @Value("${app.amqp.publisher.notifications.otp.exchange}")
    private String exchangeName;

    @Override
    public void publishSendEmailCommand(EmailNotificationPayload command) {
        if (command == null) {
            throw new NotificationCommandPublisherException("Command cannot be null");
        }

        rabbitTemplate.convertAndSend(exchangeName, routingKey, command);
    }
}
