package bankapp.auth.domain.model.exception;

public class OtpFormatException extends RuntimeException{

    public OtpFormatException(String msg) {
        super(msg);
    }
}
