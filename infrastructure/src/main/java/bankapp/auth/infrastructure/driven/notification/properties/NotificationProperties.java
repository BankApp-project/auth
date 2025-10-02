package bankapp.auth.infrastructure.driven.notification.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.amqp.publisher.notifications.otp")
public record NotificationProperties(String routingKey, String exchange) {
}
