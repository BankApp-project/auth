package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.infrastructure.crosscutting.exception.PrivateKeyCreationFailedException;
import bankapp.auth.infrastructure.crosscutting.exception.PublicKeyCreationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
@RequiredArgsConstructor
public class RSAConfiguration {

    private static final String ALGORITHM = "RSA";

    private final RSAProperties rsaProperties;

    @Bean
    public RSAPublicKey rsaPublicKey() {
        try {
            return generateRSAPublicKey();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PublicKeyCreationFailedException("Failed to create RSA public key", e);
        }
    }

    private RSAPublicKey generateRSAPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(rsaProperties.publicKeyBytes());

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        try {
            return generateRSAPrivateKey();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PrivateKeyCreationFailedException("Failed to create RSA private key", e);
        }
    }

    private RSAPrivateKey generateRSAPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(rsaProperties.privateKeyBytes());

        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }


}
