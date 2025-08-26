package bankapp.auth.services;

import bankapp.auth.application.shared.port.out.NotificationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationPort {

    private final NotificationTemplateProvider templateProvider;

    @Override
    public void sendOtpToUserEmail(EmailAddress userEmail, String otpValue) {
        templateProvider.getOtpEmailTemplate(otpValue);
    }

}
