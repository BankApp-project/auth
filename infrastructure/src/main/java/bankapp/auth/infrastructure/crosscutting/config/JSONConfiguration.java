package bankapp.auth.infrastructure.crosscutting.config;

import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.driven.otp.persistance.OtpMixin;
import bankapp.auth.infrastructure.driving.rest.serializers.UserVerificationRequirementDeserializer;
import bankapp.auth.infrastructure.driving.rest.serializers.UserVerificationRequirementSerializer;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
                .addModule(serializers())
                .build();
    }

    private Module serializers() {
        SimpleModule module = new SimpleModule("serializers");
        module.addSerializer(UserVerificationRequirement.class, new UserVerificationRequirementSerializer());
        module.addDeserializer(UserVerificationRequirement.class, new UserVerificationRequirementDeserializer());

        return module;
    }
}
