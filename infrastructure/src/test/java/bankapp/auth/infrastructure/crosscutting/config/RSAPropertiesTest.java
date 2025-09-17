package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.infrastructure.crosscutting.exception.InvalidConfigurationPropertiesException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RSAPropertiesTest {

    private static final String TEST_STRING = "Test RSA Key Content";
    private static final String VALID_BASE64_PUBLIC_KEY = Base64.getEncoder().encodeToString(TEST_STRING.getBytes());
    private static final String VALID_BASE64_PRIVATE_KEY = Base64.getEncoder().encodeToString("Private Key Content".getBytes());
    private static final String INVALID_BASE64 = "Not@Valid!Base64$String";

    @Test
    void shouldDecodePublicKeyFromValidBase64() {
        RSAProperties properties = new RSAProperties(VALID_BASE64_PUBLIC_KEY, VALID_BASE64_PRIVATE_KEY);

        byte[] result = properties.publicKeyBytes();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(TEST_STRING.getBytes());
        assertThat(new String(result)).isEqualTo(TEST_STRING);
    }

    @Test
    void shouldDecodePrivateKeyFromValidBase64() {
        RSAProperties properties = new RSAProperties(VALID_BASE64_PUBLIC_KEY, VALID_BASE64_PRIVATE_KEY);

        byte[] result = properties.privateKeyBytes();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Private Key Content".getBytes());
        assertThat(new String(result)).isEqualTo("Private Key Content");
    }

    @Test
    void shouldThrowWhenEmptyBase64Strings() {
        RSAProperties properties = new RSAProperties("", "");

        assertThrows(InvalidConfigurationPropertiesException.class, properties::publicKeyBytes);
        assertThrows(InvalidConfigurationPropertiesException.class, properties::privateKeyBytes);
    }

    @Test
    void shouldThrowExceptionForInvalidBase64PublicKey() {
        RSAProperties properties = new RSAProperties(INVALID_BASE64, VALID_BASE64_PRIVATE_KEY);

        assertThrows(IllegalArgumentException.class, properties::publicKeyBytes);
    }

    @Test
    void shouldThrowExceptionForInvalidBase64PrivateKey() {
        RSAProperties properties = new RSAProperties(VALID_BASE64_PUBLIC_KEY, INVALID_BASE64);

        assertThrows(IllegalArgumentException.class, properties::privateKeyBytes);
    }

    @Test
    void shouldHandleRealRSAKeyFormat() {
        String samplePublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA";
        String samplePrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEA";

        RSAProperties properties = new RSAProperties(samplePublicKey, samplePrivateKey);

        byte[] publicKeyBytes = properties.publicKeyBytes();
        byte[] privateKeyBytes = properties.privateKeyBytes();

        assertThat(publicKeyBytes).isNotNull();
        assertThat(privateKeyBytes).isNotNull();
        assertThat(publicKeyBytes).isNotEmpty();
        assertThat(privateKeyBytes).isNotEmpty();
    }

    @Test
    void shouldThrowWhenNullValues() {
        RSAProperties properties = new RSAProperties(null, null);

        assertThat(properties.publicKey()).isNull();
        assertThat(properties.privateKey()).isNull();

        assertThrows(InvalidConfigurationPropertiesException.class, properties::publicKeyBytes);
        assertThrows(InvalidConfigurationPropertiesException.class, properties::privateKeyBytes);
    }
}