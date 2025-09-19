package bankapp.auth.infrastructure.driven.notification;

import bankapp.payload.notification.email.otp.EmailNotificationPayload;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(EmailNotificationPayload command);
}
