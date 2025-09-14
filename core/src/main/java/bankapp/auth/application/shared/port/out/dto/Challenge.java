package bankapp.auth.application.shared.port.out.dto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public record Challenge(
        byte[] challenge,
        Instant expirationTime
) {

    public Challenge(
            byte[] challenge,
            Duration ttl,
            Clock clock
    ) {
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
        if (!(o instanceof Challenge(byte[] challengeBytes, Instant time))) return false;

        return Arrays.equals(challenge, challengeBytes) && expirationTime.equals(time);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(challenge);
        result = 31 * result + expirationTime.hashCode();
        return result;
    }
}