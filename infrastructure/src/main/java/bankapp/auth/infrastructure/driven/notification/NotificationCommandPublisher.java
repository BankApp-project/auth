package bankapp.auth.infrastructure.driven.notification;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(SendEmailNotificationCommand command);
}
