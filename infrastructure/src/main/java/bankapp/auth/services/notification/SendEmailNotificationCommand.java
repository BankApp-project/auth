package bankapp.auth.services.notification;

public record SendEmailNotificationCommand(
        String recipientEmail,
        String subject,
        String htmlBody
) {
}