package bankapp.auth.infrastructure.services.otp;

public class OtpGenerationException extends RuntimeException {
    public OtpGenerationException(String msg) {
        super(msg);
    }
}
