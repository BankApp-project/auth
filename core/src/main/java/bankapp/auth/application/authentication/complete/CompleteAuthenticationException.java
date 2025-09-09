package bankapp.auth.application.authentication.complete;

public class CompleteAuthenticationException extends RuntimeException{

    public CompleteAuthenticationException(String msg) {
        super(msg);
    }

    public CompleteAuthenticationException(String msg, Throwable e) {
        super(msg, e);
    }
}
