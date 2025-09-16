package bankapp.auth.infrastructure.driven.passkey.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.webauthn4j.WebAuthnRegistrationManager;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthn4jConfig {

    @Bean
    public ObjectConverter objectConverter() {
        return new ObjectConverter(
                new ObjectMapper(),
                new CBORMapper());
    }

    @Bean
    public WebAuthnRegistrationManager webAuthnRegistrationManager() {
        return WebAuthnRegistrationManager.createNonStrictWebAuthnRegistrationManager();
    }
}
