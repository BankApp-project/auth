package bankapp.auth.infrastructure.driven.notification;

public record EmailNotificationPayload(
        String recipientEmail,
        String subject,
        String htmlBody
) {
}