package bankapp.auth.infrastructure.driven.notification.out.amqp;

import bankapp.auth.infrastructure.driven.notification.exception.NotificationCommandPublisherException;
import bankapp.auth.infrastructure.driven.notification.out.NotificationCommandPublisher;
import bankapp.auth.infrastructure.driven.notification.properties.NotificationProperties;
import bankapp.payload.notification.email.otp.EmailNotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationCommandPublisherAmqp implements NotificationCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final NotificationProperties properties;

    @Override
    public void publishSendEmailCommand(EmailNotificationPayload command) {
        if (command == null) {
            throw new NotificationCommandPublisherException("Command cannot be null");
        }

        rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), command);
    }
}
