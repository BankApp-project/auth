package bankapp.auth.infrastructure.driven.otp.service;

public class OtpGenerationException extends RuntimeException {
    public OtpGenerationException(String msg) {
        super(msg);
    }
}
