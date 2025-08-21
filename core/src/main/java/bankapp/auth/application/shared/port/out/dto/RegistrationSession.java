package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

//omfg. Session for auth with discoverable credential wont have userId. But for auth i will get userId as userHandle, so everything is fine.
// I just need to store
public record RegistrationSession(
        UUID sessionId,          // The key for the cache
        byte[] challenge,        // The cryptographic challenge
        UUID userId,             // The user's ID
        Instant expirationTime   // When this context becomes invalid
) {

    public RegistrationSession(
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