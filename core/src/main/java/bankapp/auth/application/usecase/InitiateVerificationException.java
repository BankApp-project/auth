package bankapp.auth.application.usecase;

public class InitiateVerificationException extends RuntimeException{

    public InitiateVerificationException(String msg, Throwable e) {
        super(msg, e);
    }
}
