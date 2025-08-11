package bankapp.auth.application.shared.port.out;

public interface CommandBus {
    void sendOtpToUserEmail(String userEmail, String otpValue);
}
