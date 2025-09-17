package bankapp.auth.infrastructure.crosscutting.exception;

public class InvalidConfigurationPropertiesException extends RuntimeException {
    public InvalidConfigurationPropertiesException(String msg) {
        super(msg);
    }
}
