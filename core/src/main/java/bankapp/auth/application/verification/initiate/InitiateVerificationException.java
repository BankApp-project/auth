package bankapp.auth.application.verification.initiate;

public class InitiateVerificationException extends RuntimeException{

    public InitiateVerificationException(String msg, Throwable e) {
        super(msg, e);
    }
}
