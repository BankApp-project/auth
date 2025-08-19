package bankapp.auth.application.verification_initiate;

public class InitiateVerificationException extends RuntimeException{

    public InitiateVerificationException(String msg, Throwable e) {
        super(msg, e);
    }
}
