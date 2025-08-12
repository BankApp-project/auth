package bankapp.auth.application.shared;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class EventTemplate {
    private final UUID eventId;
    private final Instant timestamp;
    private final int version;

    public EventTemplate() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.version = 1;
    }
}
