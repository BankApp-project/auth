package bankapp.auth.infrastructure.services;

public class RegistrationConfirmAttemptException extends RuntimeException {

    public RegistrationConfirmAttemptException(String msg) {
        super(msg);
    }

}
