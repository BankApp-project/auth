package bankapp.auth.infrastructure.services.credential_options;

import bankapp.auth.application.shared.enums.AuthMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config.credential-options")
public record CredentialOptionsProperties(
        String rpId,
        AuthMode authMode) {
}
