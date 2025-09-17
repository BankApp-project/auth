package bankapp.auth.infrastructure.crosscutting.exception;

public class PublicKeyCreationFailedException extends RuntimeException {
    public PublicKeyCreationFailedException(String msg, Throwable e) {
        super(msg, e);
    }
}
