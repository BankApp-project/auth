package bankapp.auth.infrastructure.rest.verification.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

import java.util.UUID;

public record LoginResponseDto(
        PublicKeyCredentialRequestOptions options,
        UUID challengeId
) implements CompleteVerificationResponseDto {
}
