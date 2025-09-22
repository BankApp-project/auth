package bankapp.auth.infrastructure.driving.rest.verification.complete.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        description = "Response for an EXISTING user. The 'type' property will be 'login'. Contains options to initiate a passkey login."
)
public record LoginResponseDto(
        PublicKeyCredentialRequestOptions loginOptions,
        UUID sessionId
) implements CompleteVerificationResponseDto {
}
