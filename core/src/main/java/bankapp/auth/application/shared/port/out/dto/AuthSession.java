package bankapp.auth.application.shared.port.out.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthSession(
        UUID sessionId,          // The key for the cache
        byte[] challenge,        // The cryptographic challenge
        UUID userId,             // The user's ID
        Instant expirationTime   // When this context becomes invalid
) {
}