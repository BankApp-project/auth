package bankapp.auth.services.otp;

public class OtpGenerationException extends RuntimeException {
    public OtpGenerationException(String msg) {
        super(msg);
    }
}
