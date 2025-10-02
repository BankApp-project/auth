package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.application.shared.port.out.service.NotificationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.notification.dto.EmailTemplate;
import bankapp.auth.infrastructure.driven.notification.out.NotificationCommandPublisher;
import bankapp.payload.notification.email.otp.EmailNotificationPayload;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationPort {

    private final NotificationTemplateProvider templateProvider;
    private final NotificationCommandPublisher notificationCommandPublisher;

    @Override
    public void sendOtpToUserEmail(@NonNull EmailAddress userEmail, @NonNull String otpValue) {
        log.debug("Sending OTP notification to user email.");

        var template = getOptEmailTemplate(userEmail, otpValue);

        var command = getSendEmailNotificationCommand(template);
        notificationCommandPublisher.publishSendEmailCommand(command);
        log.debug("Successfully sent OTP notification to user email.");
    }

    private EmailTemplate getOptEmailTemplate(EmailAddress emailAddress, String otpValue) {
        return templateProvider.getOtpEmailTemplate(emailAddress, otpValue);
    }

    private EmailNotificationPayload getSendEmailNotificationCommand(EmailTemplate template) {
        String email = template.sendTo().getValue();
        return new EmailNotificationPayload(
                email,
                template.subject(),
                template.body());
    }
}
