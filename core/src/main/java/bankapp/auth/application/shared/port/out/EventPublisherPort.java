package bankapp.auth.application.shared.port.out;

public interface EventPublisherPort {
    void publish(Object object);
}
