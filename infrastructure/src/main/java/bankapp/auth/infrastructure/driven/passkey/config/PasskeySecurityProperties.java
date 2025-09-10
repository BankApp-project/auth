package bankapp.auth.infrastructure.driven.passkey.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config.passkey.security")
public record PasskeySecurityProperties(
        boolean userVerificationRequired,
        boolean userPresenceRequired
) {
}