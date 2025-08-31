package bankapp.auth.infrastructure.services.credential_options;

import bankapp.auth.application.shared.enums.AuthMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.config.credential-options")
public record CredentialOptionsProperties(
        String rpId,
        Duration timeout,
        AuthMode authMode) {
}
