package bankapp.auth.infrastructure.services.notification;

public class NotificationCommandPublisherException extends RuntimeException {

    public NotificationCommandPublisherException(String msg) {
        super(msg);
    }
}
