package bankapp.auth.infrastructure.driven.passkey.service;

public class AuthenticationConfirmAttemptException extends RuntimeException {

    public AuthenticationConfirmAttemptException(String msg, Throwable e) {
        super(msg, e);
    }
}
