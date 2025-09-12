package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.driven.otp.persistance.OtpMixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JSONConfiguration {

    @Bean(name = "objectMapper")
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean(name = "cborMapper")
    public ObjectMapper cborMapper() {
        return new ObjectMapper(new CBORFactory());
    }

    @Bean
    public tools.jackson.databind.json.JsonMapper jsonMapper() {
        return tools.jackson.databind.json.JsonMapper.builder()
                .addModule(new tools.jackson.datatype.jsr310.JavaTimeModule())
                .addMixIn(Otp.class, OtpMixin.class)
                .build();
    }
}
