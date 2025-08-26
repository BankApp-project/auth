package bankapp.auth.services;

public record SendEmailNotificationCommand(
        String recipientEmail,
        String subject,
        String htmlBody
) {
}