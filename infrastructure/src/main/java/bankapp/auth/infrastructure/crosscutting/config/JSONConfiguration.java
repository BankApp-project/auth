package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.driven.otp.persistance.OtpMixin;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JSONConfiguration {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new Jdk8Module())
                .addMixIn(Otp.class, OtpMixin.class)
                .defaultBase64Variant(Base64Variants.MODIFIED_FOR_URL)
                .build();
    }
}
