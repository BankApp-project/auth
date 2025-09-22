package bankapp.auth.infrastructure.driving.rest.verification.complete.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// --- Response DTOs ---
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginResponseDto.class, name = "login"),
        @JsonSubTypes.Type(value = RegistrationResponseDto.class, name = "registration")
})
@Schema(
        description = "A polymorphic response for the verification result. The 'type' property identifies the specific flow.",
        discriminatorProperty = "type"
)

public interface CompleteVerificationResponseDto {
    @Schema(description = "A unique session identifier for tracking the user's verification state.")
    UUID sessionId();
}

