package bankapp.auth.infrastructure.driven.notification;

public record SendEmailNotificationCommand(
        String recipientEmail,
        String subject,
        String htmlBody
) {
}