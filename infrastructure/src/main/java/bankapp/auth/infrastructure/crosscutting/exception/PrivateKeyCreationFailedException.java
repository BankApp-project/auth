package bankapp.auth.infrastructure.crosscutting.exception;

public class PrivateKeyCreationFailedException extends RuntimeException {
    public PrivateKeyCreationFailedException(String msg, Throwable e) {
        super(msg, e);
    }
}
