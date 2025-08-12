package bankapp.auth.application.initiate_verification.exception;

public class InitiateVerificationException extends RuntimeException{

    public InitiateVerificationException(String msg, Throwable e) {
        super(msg, e);
    }
}
