package bankapp.auth.infrastructure.driven.passkey.exception;

public class AuthenticationConfirmAttemptException extends RuntimeException {

    public AuthenticationConfirmAttemptException(String msg, Throwable e) {
        super(msg, e);
    }
}
