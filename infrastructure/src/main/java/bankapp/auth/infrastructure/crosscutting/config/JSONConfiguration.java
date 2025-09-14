package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.driven.otp.persistance.OtpMixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JSONConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new Jdk8Module())
                .build();
    }

    @Bean
    public tools.jackson.databind.json.JsonMapper jsonMapper() {
        return tools.jackson.databind.json.JsonMapper.builder()
                .addModule(new tools.jackson.datatype.jsr310.JavaTimeModule())
                .addMixIn(Otp.class, OtpMixin.class)
                .build();
    }
}
