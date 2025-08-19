package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public record AuthSession(
        UUID sessionId,          // The key for the cache
        byte[] challenge,        // The cryptographic challenge
        UUID userId,             // The user's ID
        Instant expirationTime   // When this context becomes invalid
) {

    public AuthSession(
            UUID sessionId,
            byte[] challenge,
            UUID userId,
            long ttlInSeconds,
            Clock clock
    ) {
        this(
                sessionId,
                challenge,
                userId,
                Instant.now(clock).plusSeconds(ttlInSeconds)
        );
    }
    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(expirationTime);
    }
}