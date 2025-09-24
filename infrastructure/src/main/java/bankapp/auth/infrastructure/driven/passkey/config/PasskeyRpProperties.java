package bankapp.auth.infrastructure.driven.passkey.config;

import bankapp.auth.application.shared.enums.AuthMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "app.config.passkey.rp")
public record PasskeyRpProperties(
        String rpId,
        String origin,
        AuthMode authMode
) {
    @ConstructorBinding
    public PasskeyRpProperties {

    }

    public PasskeyRpProperties(String rpId, AuthMode authMode) {
        var origin = "https://" + rpId;
        this(rpId, origin, authMode);
    }
}
