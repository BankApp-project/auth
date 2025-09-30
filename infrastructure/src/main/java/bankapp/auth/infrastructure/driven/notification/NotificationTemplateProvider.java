package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateProvider {

    private static final String SUPPORT_EMAIL_ADDRESS = "support@bankapp.online";
    private final OtpEmailTemplateProvider otpEmailTemplateProvider = new OtpEmailTemplateProvider(SUPPORT_EMAIL_ADDRESS);

    public String getOtpEmailTemplate(String otp) {
        return String.format("Hello! \n Your One-Time Password is: %s", otp);
    }

    public EmailTemplate getOtpEmailTemplate(EmailAddress email, String otp) {
        return otpEmailTemplateProvider.get(email, otp);
    }
}
