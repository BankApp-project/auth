package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class Otp {

    private final UUID id = UUID.randomUUID();
    private final String key;
    private final String value;

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
    public String toString() {
        // Avoid logging the actual OTP value and keys in production for security.
        return "OTP[value=******, key=" + (key != null ? key.substring(0, Math.min(3, key.length())) + "..." : "null") + "]";
    }
}
