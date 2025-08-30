package bankapp.auth.infrastructure.rest.verification.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;

import java.util.UUID;

public record RegistrationResponseDto(
        PublicKeyCredentialCreationOptions options,
        UUID challengeId
) implements CompleteVerificationResponseDto {
}
