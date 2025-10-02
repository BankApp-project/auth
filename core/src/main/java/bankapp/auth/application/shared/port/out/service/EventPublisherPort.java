package bankapp.auth.application.shared.port.out.service;

import bankapp.auth.application.shared.EventTemplate;

public interface EventPublisherPort {
    void publish(EventTemplate object);
}
