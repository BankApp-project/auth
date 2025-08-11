package bankapp.auth.application.port.out;

public interface EventPublisherPort {
    void publish(Object object);
}
