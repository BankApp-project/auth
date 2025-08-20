package bankapp.auth.application.registration_complete;

public class CompleteRegistrationException extends RuntimeException{
    public CompleteRegistrationException(String msg, Throwable e) {
        super(msg, e);
    }

    public CompleteRegistrationException(String msg) {
        super(msg);
    }
}