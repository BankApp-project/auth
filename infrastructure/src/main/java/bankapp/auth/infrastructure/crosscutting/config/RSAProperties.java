package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.domain.model.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "app.config.rsa-key")
public record RSAProperties(
        @NotNull RSAPublicKey publicKey,
        @NotNull RSAPrivateKey privateKey) {
}
