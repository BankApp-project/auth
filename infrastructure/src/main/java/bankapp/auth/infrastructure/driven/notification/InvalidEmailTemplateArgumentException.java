package bankapp.auth.infrastructure.driven.notification;

public class InvalidEmailTemplateArgumentException extends RuntimeException {

    public InvalidEmailTemplateArgumentException(String msg) {
        super(msg);
    }
}
