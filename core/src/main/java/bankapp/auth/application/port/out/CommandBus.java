package bankapp.auth.application.port.out;

public interface CommandBus {
    void sendOtpToUserEmail(String userEmail, String otpValue);
}
