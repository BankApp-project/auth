package bankapp.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuthSession(
        UUID sessionId,       // The key for the cache
        byte[] challenge,        // The cryptographic challenge
        UUID userId,             // The user's stable ID (can be null for new registrations)
        Instant expirationTime   // When this context becomes invalid
) {
}