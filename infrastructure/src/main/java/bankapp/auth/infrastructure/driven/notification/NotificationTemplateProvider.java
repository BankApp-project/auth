package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class NotificationTemplateProvider {

    public String getOtpEmailTemplate(String otp) {
        return String.format("Hello! \n Your One-Time Password is: %s", otp);
    }

    public EmailTemplate getOtpEmailTemplate(EmailAddress email, String otp, Duration timeout) {
        validateArguments(otp, timeout);
        String body = getOtpEmailBody(otp, timeout.toMinutes());
        String subject = getOtpEmailSubject();

        return new EmailTemplate(subject, body, email);

    }

    private String getOtpEmailSubject() {
        return "Your BankApp Verification Code";
    }

    private String getOtpEmailBody(String otp, long durationInMinutes) {
        return """
                Hello,
                
                Your One-Time Password (OTP) for BankApp is:
                
                %s
                
                This code will expire in %d minutes.
                
                For your security:
                - Never share this code with anyone
                - BankApp staff will never ask for your OTP
                - If you didn't request this code, please ignore this email
                
                Need help? Contact us at support@bankapp.com
                
                ---
                BankApp Team
                """.formatted(otp, durationInMinutes);
    }

    private void validateArguments(String otp, Duration timeout) {
        if (otp == null || otp.isBlank()) {
            throw new InvalidEmailTemplateArgumentException("Otp cannot be empty");
        }
        if (timeout == null) {
            throw new InvalidEmailTemplateArgumentException("Timeout cannot be null");
        }
    }

}
