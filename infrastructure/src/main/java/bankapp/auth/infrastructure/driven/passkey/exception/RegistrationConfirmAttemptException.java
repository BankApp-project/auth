package bankapp.auth.infrastructure.driven.passkey.exception;

public class RegistrationConfirmAttemptException extends RuntimeException {

    public RegistrationConfirmAttemptException(String msg) {
        super(msg);
    }

}
