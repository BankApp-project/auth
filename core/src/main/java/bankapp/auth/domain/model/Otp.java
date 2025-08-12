package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
//extend this class to make persistable variant
public class Otp {

    private final UUID id = UUID.randomUUID();
    private final String key;
    private final String value;
    @Setter
    private Instant validationTime;

    public Otp( String value, String key) {
        this.value = Objects.requireNonNull(value, "OTP value cannot be null");
        this.key = Objects.requireNonNull(key, "OTP key cannot be null");

        if (value.trim().isEmpty()) {
            throw new OtpFormatException("OTP value cannot be empty");
        }

        if (key.trim().isEmpty()) {
            throw new OtpFormatException("OTP key cannot be empty");
        }
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
}
