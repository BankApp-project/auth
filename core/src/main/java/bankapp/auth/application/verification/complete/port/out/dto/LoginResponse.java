package bankapp.auth.application.verification.complete.port.out.dto;

import bankapp.auth.application.verification.complete.CompleteVerificationResponse;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

import java.util.UUID;

public record LoginResponse(
        PublicKeyCredentialRequestOptions options,
        UUID challengeId
) implements CompleteVerificationResponse {
}
