package bankapp.auth.domain.model.exception;

public class InvalidEmailFormatException extends RuntimeException{

    public InvalidEmailFormatException(String msg) {
        super(msg);
    }
}
