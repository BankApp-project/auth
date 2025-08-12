package bankapp.auth.application.shared.port.out;

public interface NotificationPort {
    void sendOtpToUserEmail(String userEmail, String otpValue);
}
