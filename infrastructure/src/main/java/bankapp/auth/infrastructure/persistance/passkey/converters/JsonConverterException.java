package bankapp.auth.infrastructure.persistance.passkey.converters;

public class JsonConverterException extends RuntimeException{

    public JsonConverterException(String msg) {
        super(msg);
    }
}
