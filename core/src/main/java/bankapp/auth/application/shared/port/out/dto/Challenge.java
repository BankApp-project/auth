package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public record Challenge(
        UUID sessionId,          // The key for the cache
        byte[] value,            // The cryptographic value
        Instant expirationTime   // When this context becomes invalid
) {

    public Challenge(
            UUID sessionId,
            byte[] value,
            Duration ttl,
            Clock clock
    ) {
        this(
                sessionId,
                value,
                Instant.now(clock).plus(ttl)
        );
    }

    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(expirationTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Challenge challenge)) return false;

        return Arrays.equals(value, challenge.value) && sessionId.equals(challenge.sessionId) && expirationTime.equals(challenge.expirationTime);
    }
    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        result = 31 * result + expirationTime.hashCode();
        return result;
    }
}