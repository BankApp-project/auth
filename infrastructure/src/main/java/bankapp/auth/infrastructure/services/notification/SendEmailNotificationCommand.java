package bankapp.auth.infrastructure.services.notification;

public record SendEmailNotificationCommand(
        String recipientEmail,
        String subject,
        String htmlBody
) {
}