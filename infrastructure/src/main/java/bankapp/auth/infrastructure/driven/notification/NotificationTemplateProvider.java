package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.notification.dto.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationTemplateProvider {

    private static final String SUPPORT_EMAIL_ADDRESS = "support@bankapp.online";
    private final OtpEmailTemplateProvider otpEmailTemplateProvider = new OtpEmailTemplateProvider(SUPPORT_EMAIL_ADDRESS);

    public EmailTemplate getOtpEmailTemplate(EmailAddress email, String otp) {
        log.info("Generating OTP email template.");

        EmailTemplate template = otpEmailTemplateProvider.get(email, otp);
        log.info("Successfully generated OTP email template.");
        return template;
    }
}
