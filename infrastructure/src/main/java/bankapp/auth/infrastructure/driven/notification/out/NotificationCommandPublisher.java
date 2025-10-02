package bankapp.auth.infrastructure.driven.notification.out;

import bankapp.payload.notification.email.otp.EmailNotificationPayload;

public interface NotificationCommandPublisher {

    void publishSendEmailCommand(EmailNotificationPayload command);
}
