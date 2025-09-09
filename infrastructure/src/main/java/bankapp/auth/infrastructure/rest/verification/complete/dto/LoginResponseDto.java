package bankapp.auth.infrastructure.rest.verification.complete.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

import java.util.UUID;

public record LoginResponseDto(
        PublicKeyCredentialRequestOptions loginOptions,
        UUID challengeId
) implements CompleteVerificationResponseDto {
}
