package bankapp.auth.infrastructure.driven.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateProvider {

    public String getOtpEmailTemplate(String otp) {
        return String.format("Hello! /n Your One-Time Password is: %s", otp);
    }

}
