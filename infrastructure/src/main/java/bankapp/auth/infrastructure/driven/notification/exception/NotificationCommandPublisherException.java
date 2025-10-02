package bankapp.auth.infrastructure.driven.notification.exception;

public class NotificationCommandPublisherException extends RuntimeException {

    public NotificationCommandPublisherException(String msg) {
        super(msg);
    }
}
