package bankapp.auth.persistance.otp;

import bankapp.auth.domain.model.Otp;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.Instant;

public abstract class OtpMixin {

    /**
     * This annotation tells Jackson: "Use this constructor to create the object."
     * The @JsonProperty annotations map the JSON field names ("key", "value", etc.)
     * to the constructor parameters.
     */
    @JsonCreator
    public static Otp reconstitute(
            @JsonProperty("key") String key,
            @JsonProperty("value") String value,
            @JsonProperty("expirationTime") Instant expirationTime,
            @JsonProperty("ttl") Duration ttl
    ) {
        return null;
    }
}
