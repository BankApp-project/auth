package bankapp.auth.application.verification.complete;

public class OtpVerificationException extends RuntimeException {

    public OtpVerificationException(String msg) {
        super(msg);
    }
}
