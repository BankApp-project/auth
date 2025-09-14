package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public record Session(
        UUID sessionId,          // The key for the cache
        byte[] challenge,            // The cryptographic challenge
        Instant expirationTime,  // When this context becomes invalid
        UUID userId             // ID of the related user
) {

    public Session(
            UUID sessionId,
            byte[] value,
            Duration ttl,
            Clock clock,
            UUID userId
    ) {
        this(
                sessionId,
                value,
                Instant.now(clock).plus(ttl),
                userId
        );
    }

    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(expirationTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Session(UUID id, byte[] value1, Instant time, UUID user))) return false;

        return Arrays.equals(challenge, value1) && sessionId.equals(id) && expirationTime.equals(time) && userId.equals(user);
    }
    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + Arrays.hashCode(challenge);
        result = 31 * result + expirationTime.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}