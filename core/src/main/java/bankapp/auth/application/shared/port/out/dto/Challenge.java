package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public record Challenge(
        UUID sessionId,          // The key for the cache
        byte[] value,            // The cryptographic value
        Instant expirationTime   // When this context becomes invalid
) {

    public Challenge(
            UUID sessionId,
            byte[] value,
            long ttlInSeconds,
            Clock clock
    ) {
        this(
                sessionId,
                value,
                Instant.now(clock).plusSeconds(ttlInSeconds)
        );
    }
    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(expirationTime);
    }
}