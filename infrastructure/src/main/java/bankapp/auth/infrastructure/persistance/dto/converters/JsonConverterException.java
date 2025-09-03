package bankapp.auth.infrastructure.persistance.dto.converters;

public class JsonConverterException extends RuntimeException{

    public JsonConverterException(String msg) {
        super(msg);
    }
}
