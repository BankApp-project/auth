package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import lombok.Getter;
import lombok.NonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Getter
public class Otp {

    private final String key;
    private final String value;

    private final Instant expirationTime;
    private final Duration ttl;

    private Otp(String key, String value, Instant expirationTime, Duration ttl) {

        this.value = Objects.requireNonNull(value, "OTP value cannot be null");
        this.key = Objects.requireNonNull(key, "OTP key cannot be null");
        this.expirationTime = Objects.requireNonNull(expirationTime, "Expiration time cannot be null");
        this.ttl = Objects.requireNonNull(ttl, "TTL cannot be null");

        if (value.trim().isEmpty()) {
            throw new OtpFormatException("OTP value cannot be empty");
        }

        if (key.trim().isEmpty()) {
            throw new OtpFormatException("OTP key cannot be empty");
        }
    }

    public static Otp createNew(String key, String value, @NonNull Clock clock, long ttlInSeconds) {
        return new Otp(
                key,
                value,
                Instant.now(clock).plusSeconds(ttlInSeconds),
                Duration.ofSeconds(ttlInSeconds)
        );
    }

    public static Otp reconstitute(String key, String value, Instant expirationTime, Duration ttl) {
        return new Otp(
                key,
                value,
                expirationTime,
                ttl
        );
    }

    @Override
    public final boolean equals(Object object) {
        if (!(object instanceof Otp otp)) return false;

        return key.equals(otp.key) && value.equals(otp.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // Avoid logging the actual OTP value and keys in production for security.
        return "OTP[value=******, key=" + (key != null ? key.substring(0, Math.min(3, key.length())) + "..." : "null") + "]";
    }

    public boolean isValid(Clock clock) {
        return Instant.now(clock).isBefore(this.getExpirationTime());
    }

}
