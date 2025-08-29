package bankapp.auth.application.verification_complete;

public class OtpVerificationException extends RuntimeException {

    public OtpVerificationException(String msg) {
        super(msg);
    }
}
