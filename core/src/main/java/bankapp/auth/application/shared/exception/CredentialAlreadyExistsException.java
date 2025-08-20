package bankapp.auth.application.shared.exception;

/**
 * This exception is thrown to indicate that a credential already exists,
 * typically when attempting to save a duplicate credential.
 * <p>
 * It should be thrown when PublicKey or CredentialId are duplicated
 */
public class CredentialAlreadyExistsException extends RuntimeException {

    public CredentialAlreadyExistsException(String msg) {
        super(msg);
    }

    public CredentialAlreadyExistsException(String msg, Throwable e) {
        super(msg, e);
    }
}
