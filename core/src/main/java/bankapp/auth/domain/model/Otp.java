package bankapp.auth.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class Otp {

    private final UUID id = UUID.randomUUID();
    private final String key;
    private final String value;

    public Otp(String value, String key) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        // Avoid logging the actual OTP value and keys in production for security.
        return "OTP[value=******, key=" + (key != null ? key.substring(0, Math.min(3, key.length())) + "..." : "null") + "]";
    }
}
