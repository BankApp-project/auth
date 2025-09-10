package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.enums.AuthMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config.credential-options")
public record CredentialOptionsProperties(
        String rpId,
        AuthMode authMode) {
}
