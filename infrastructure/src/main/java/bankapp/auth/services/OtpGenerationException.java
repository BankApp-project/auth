package bankapp.auth.services;

public class OtpGenerationException extends RuntimeException {
    public OtpGenerationException(String msg) {
        super(msg);
    }
}
