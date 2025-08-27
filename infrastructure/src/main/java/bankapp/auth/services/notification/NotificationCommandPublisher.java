package bankapp.auth.services.notification;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(SendEmailNotificationCommand command);
}
