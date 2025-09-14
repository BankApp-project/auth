package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.util.UUID;

public record Session(
        UUID sessionId,          // The key for the cache
        Challenge challenge,     // The challenge with expiration time
        UUID userId    // ID of the related user
) {

//    public Session(UUID sessionId, Challenge challenge, UUID userId) {
//        this(
//                sessionId,
//                challenge,
//                Optional.of(userId)
//        );
//    }
//
//    public Session(UUID sessionId, Challenge challenge) {
//        this(
//                sessionId,
//                challenge,
//                Optional.empty()
//        );
//    }

    public boolean isValid(Clock clock) {
        return challenge.isValid(clock);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Session session)) return false;

        return java.util.Objects.equals(userId, session.userId) && sessionId.equals(session.sessionId) && challenge.equals(session.challenge);
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + challenge.hashCode();
        result = 31 * result + java.util.Objects.hashCode(userId);
        return result;
    }
}