package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class NotificationTemplateProvider {

    public String getOtpEmailTemplate(String otp) {
        return String.format("Hello! /n Your One-Time Password is: %s", otp);
    }

    public OtpEmailTemplate getOtpEmailTemplate(EmailAddress email, String otp, Duration timeout) {

        throw new InvalidEmailTemplateArgumentException("Otp cannot be empty");
    }

}
