package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Getter
public class Otp {

    private final String key;
    private final String value;

    private Instant expirationTime;

    @Deprecated
    public Otp(String value, String key) {
        this.value = Objects.requireNonNull(value, "OTP value cannot be null");
        this.key = Objects.requireNonNull(key, "OTP key cannot be null");

        if (value.trim().isEmpty()) {
            throw new OtpFormatException("OTP value cannot be empty");
        }

        if (key.trim().isEmpty()) {
            throw new OtpFormatException("OTP key cannot be empty");
        }
    }

    public Otp(String key, String value, Instant expirationTime) {
        this(value, key);
        if (expirationTime == null) {
            throw new NullPointerException("Expiration time cannot be null");
        }
        this.expirationTime = expirationTime;
    }

    public Otp(String key, String value, Clock clock, Long ttl) {
        this(key, value, Instant.now(clock).plusSeconds(ttl));
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
