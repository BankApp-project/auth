package bankapp.auth.infrastructure.crosscutting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
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
}
