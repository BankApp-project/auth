package bankapp.auth.infrastructure.driving.rest.verification.complete.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        description = "Response for a NEW user. The 'type' property will be 'registration'. Contains options to create a new passkey."
)
public record RegistrationResponseDto(
        PublicKeyCredentialCreationOptions registrationOptions,
        UUID sessionId
) implements CompleteVerificationResponseDto {
}
