package bankapp.auth.services;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(SendEmailNotificationCommand command);
}
