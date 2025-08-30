package bankapp.auth.infrastructure.services.notification;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(SendEmailNotificationCommand command);
}
