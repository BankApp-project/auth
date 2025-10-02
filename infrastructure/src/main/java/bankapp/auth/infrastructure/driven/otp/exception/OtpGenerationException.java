package bankapp.auth.infrastructure.driven.otp.exception;

public class OtpGenerationException extends RuntimeException {
    public OtpGenerationException(String msg) {
        super(msg);
    }
}
