package bankapp.auth.application.verification_complete;

public class CompleteVerificationException extends RuntimeException {

    public CompleteVerificationException(String msg) {
        super(msg);
    }

    public  CompleteVerificationException(String msg, Throwable e) {
        super(msg, e);
    }
}
