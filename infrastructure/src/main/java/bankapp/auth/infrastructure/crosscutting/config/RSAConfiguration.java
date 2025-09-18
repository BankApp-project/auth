package bankapp.auth.infrastructure.crosscutting.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class RSAConfiguration {

    private final RSAProperties rsaProperties;

    @Bean
    public RSAPublicKey rsaPublicKey() {
        return rsaProperties.publicKey();
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        return rsaProperties.privateKey();
    }
}
