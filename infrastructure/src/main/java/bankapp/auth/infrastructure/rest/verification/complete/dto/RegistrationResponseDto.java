package bankapp.auth.infrastructure.rest.verification.complete.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;

import java.util.UUID;

public record RegistrationResponseDto(
        PublicKeyCredentialCreationOptions registrationOptions,
        UUID challengeId
) implements CompleteVerificationResponseDto {
}
