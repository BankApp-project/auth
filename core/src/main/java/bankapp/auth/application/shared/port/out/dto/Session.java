package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

public record Session(
        UUID sessionId,          // The key for the cache
        Challenge challenge,     // The challenge with expiration time
        UUID userId             // ID of the related user
) {

    public Session(
            UUID sessionId,
            byte[] challengeValue,
            Duration ttl,
            Clock clock,
            UUID userId
    ) {
        this(
                sessionId,
                new Challenge(challengeValue, ttl, clock),
                userId
        );
    }

    public boolean isValid(Clock clock) {
        return challenge.isValid(clock);
    }

    public java.time.Instant expirationTime() {
        return challenge.expirationTime();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Session session)) return false;

        return userId.equals(session.userId) && sessionId.equals(session.sessionId) && challenge.equals(session.challenge);
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + challenge.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}