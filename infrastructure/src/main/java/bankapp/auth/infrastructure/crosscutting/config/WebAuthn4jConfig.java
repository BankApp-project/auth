package bankapp.auth.infrastructure.crosscutting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthn4jConfig {

    private final ObjectMapper objectMapper;
    private final ObjectMapper cborMapper;

    public WebAuthn4jConfig(
            @Qualifier("objectMapper") ObjectMapper objectMapper,
            @Qualifier("cborMapper") ObjectMapper cborMapper
    ) {
        this.objectMapper = objectMapper;
        this.cborMapper = cborMapper;
    }

    @Bean
    public ObjectConverter objectConverter() {
        return new ObjectConverter(objectMapper, cborMapper);
    }
}
