package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.infrastructure.crosscutting.exception.InvalidConfigurationPropertiesException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;

@ConfigurationProperties(prefix = "app.config.rsa-key")
public record RSAProperties(
        @NotNull String publicKey,
        @NotNull String privateKey) {

    public byte[] publicKeyBytes() {
        if (publicKey == null || publicKey.isBlank()) {
            throw new InvalidConfigurationPropertiesException("Public key cannot be blank or null");
        }
        return Base64.getDecoder().decode(publicKey);
    }

    public byte[] privateKeyBytes() {
        if (privateKey == null || privateKey.isBlank()) {
            throw new InvalidConfigurationPropertiesException("Public key cannot be blank or null");
        }
        return Base64.getDecoder().decode(privateKey);
    }
}
