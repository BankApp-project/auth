package bankapp.auth.application.shared.port.out;

import bankapp.auth.domain.model.vo.EmailAddress;

public interface NotificationPort {
    //implementation should generate email template and send it to NotificationService
    void sendOtpToUserEmail(EmailAddress userEmail, String otpValue);
}
