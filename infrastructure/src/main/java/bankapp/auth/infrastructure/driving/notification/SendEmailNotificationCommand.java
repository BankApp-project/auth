package bankapp.auth.infrastructure.driving.notification;

public record SendEmailNotificationCommand(
        String recipientEmail,
        String subject,
        String htmlBody
) {
}