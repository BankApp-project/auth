package bankapp.auth.application.shared.port.out.dto;

import bankapp.auth.domain.model.annotations.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public record Challenge(
        @NotNull byte[] challenge,
        @NotNull Instant expirationTime
) {
    public Challenge {
        Objects.requireNonNull(challenge, "Challenge cannot be null");
        Objects.requireNonNull(expirationTime, "Expiration time cannot be null");

        if (challenge.length == 0) {
            throw new IllegalArgumentException("Challenge cannot be empty");
        }
        if (expirationTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expiration time cannot be in the past");
        }
    }

    public Challenge(
            @NotNull byte[] challenge,
            @NotNull Duration ttl,
            @NotNull Clock clock
    ) {
        Objects.requireNonNull(ttl, "TTL cannot be null");
        Objects.requireNonNull(clock, "Clock cannot be null");

        this(
                challenge,
                Instant.now(clock).plus(ttl)
        );
    }

    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(expirationTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Challenge(byte[] challenge1, Instant time))) return false;

        return Arrays.equals(challenge, challenge1) && expirationTime.equals(time);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(challenge);
        result = 31 * result + expirationTime.hashCode();
        return result;
    }
}