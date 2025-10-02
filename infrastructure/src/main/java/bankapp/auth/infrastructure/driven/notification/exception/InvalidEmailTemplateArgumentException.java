package bankapp.auth.infrastructure.driven.notification.exception;

public class InvalidEmailTemplateArgumentException extends RuntimeException {

    public InvalidEmailTemplateArgumentException(String msg) {
        super(msg);
    }
}
