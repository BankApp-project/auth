package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.application.shared.port.out.NotificationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationPort {

    public static final String SUBJECT = "Your OTP code for BankApp is here!";

    private final NotificationTemplateProvider templateProvider;
    private final NotificationCommandPublisher notificationCommandPublisher;

    @Override
    public void sendOtpToUserEmail(@NonNull EmailAddress userEmail, @NonNull String otpValue) {
        var template = getOtpEmailTemplate(otpValue);

        var command = getSendEmailNotificationCommand(userEmail, template);
        notificationCommandPublisher.publishSendEmailCommand(command);
    }

    private String getOtpEmailTemplate(String otpValue) {
        return templateProvider.getOtpEmailTemplate(otpValue);
    }

    private EmailNotificationPayload getSendEmailNotificationCommand(EmailAddress emailAddress, String template) {
        String email = emailAddress.getValue();
        return new EmailNotificationPayload(email, SUBJECT, template);
    }
}
