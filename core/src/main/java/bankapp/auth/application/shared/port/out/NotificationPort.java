package bankapp.auth.application.shared.port.out;

public interface NotificationPort {
    //implementation should generate email template and send it to NotificationService
    void sendOtpToUserEmail(String userEmail, String otpValue);
}
