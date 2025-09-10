package bankapp.auth.infrastructure.driving.notification;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(SendEmailNotificationCommand command);
}
