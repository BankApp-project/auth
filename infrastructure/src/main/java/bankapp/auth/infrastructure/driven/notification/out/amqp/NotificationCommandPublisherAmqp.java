package bankapp.auth.infrastructure.driven.notification.out.amqp;

import bankapp.auth.infrastructure.driven.notification.config.NotificationProperties;
import bankapp.auth.infrastructure.driven.notification.exception.NotificationCommandPublisherException;
import bankapp.auth.infrastructure.driven.notification.out.NotificationCommandPublisher;
import bankapp.payload.notification.email.otp.EmailNotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCommandPublisherAmqp implements NotificationCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final NotificationProperties properties;

    @Override
    public void publishSendEmailCommand(EmailNotificationPayload command) {
        log.info("Publishing email notification command to message broker.");
        log.debug("Publishing to exchange: {} with routing key: {}", properties.exchange(), properties.routingKey());

        if (command == null) {
            log.error("Failed to publish email notification command: command is null");
            throw new NotificationCommandPublisherException("Command cannot be null");
        }

        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), command);
            log.info("Successfully published email notification command.");
        } catch (Exception ex) {
            log.error("Failed to publish email notification command to exchange: {}", properties.exchange(), ex);
            throw ex;
        }
    }
}
