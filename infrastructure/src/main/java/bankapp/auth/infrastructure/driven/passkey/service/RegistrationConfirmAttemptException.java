package bankapp.auth.infrastructure.driven.passkey.service;

public class RegistrationConfirmAttemptException extends RuntimeException {

    public RegistrationConfirmAttemptException(String msg) {
        super(msg);
    }

}
