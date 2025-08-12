package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.EventTemplate;

public interface EventPublisherPort {
    void publish(EventTemplate object);
}
