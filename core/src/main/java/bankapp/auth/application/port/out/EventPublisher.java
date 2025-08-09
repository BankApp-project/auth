package bankapp.auth.application.port.out;

public interface EventPublisher {
    void publish(Object object);
}
