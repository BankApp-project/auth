package bankapp.auth.infrastructure.driven.passkey.config;

import bankapp.auth.application.shared.enums.AuthMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config.passkey.rp")
public record PasskeyRpProperties(
        String rpId,
        AuthMode authMode
) {
}
