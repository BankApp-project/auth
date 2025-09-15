package bankapp.auth.application.shared.port.out.dto;

import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.Nullable;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record Session(
        @NotNull UUID sessionId,                     // The key for the cache
        @NotNull Challenge challenge,                // The challenge with expiration time
        @NotNull Optional<UUID> userId,              // ID of the related user
        @NotNull Optional<List<UUID>> credentialId   // ID of the related credential record
) {

    public Session {
        Objects.requireNonNull(sessionId, "Session ID cannot be null");
        Objects.requireNonNull(challenge, "Challenge cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(credentialId, "Credential ID cannot be null");
    }

    public Session(UUID sessionId, Challenge challenge, @Nullable UUID userId, @Nullable List<UUID> credentialId) {
        this(
                sessionId,
                challenge,
                Optional.ofNullable(userId),
                Optional.ofNullable(credentialId)
        );
    }

    public Session(UUID sessionId, Challenge challenge, @Nullable UUID userId) {
        this(
                sessionId,
                challenge,
                Optional.ofNullable(userId),
                Optional.empty()
        );
    }

    public Session(UUID sessionId, Challenge challenge) {
        this(
                sessionId,
                challenge,
                Optional.empty(),
                Optional.empty()
        );
    }

    public boolean isValid(Clock clock) {
        return challenge.isValid(clock);
    }
}