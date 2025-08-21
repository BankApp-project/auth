package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public record LoginSession(
        UUID sessionId,          // The key for the cache
        byte[] challenge,        // The cryptographic challenge
        Instant expirationTime   // When this context becomes invalid
) {
    public LoginSession(
            UUID sessionId,
            byte[] challenge,
            long ttlInSeconds,
            Clock clock
    ) {
        this(
                sessionId,
                challenge,
                Instant.now(clock).plusSeconds(ttlInSeconds)
        );
    }
    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(expirationTime);
    }
}
