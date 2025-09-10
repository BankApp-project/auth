package bankapp.auth.infrastructure.driven.webauthn.service;

public class RegistrationConfirmAttemptException extends RuntimeException {

    public RegistrationConfirmAttemptException(String msg) {
        super(msg);
    }

}
