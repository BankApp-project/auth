package bankapp.auth.infrastructure.rest.verification.complete.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

// The DTO interface WITH annotations
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginResponseDto.class, name = "login"),
        @JsonSubTypes.Type(value = RegistrationResponseDto.class, name = "registration")
})
public interface CompleteVerificationResponseDto {
    UUID challengeId();
}
