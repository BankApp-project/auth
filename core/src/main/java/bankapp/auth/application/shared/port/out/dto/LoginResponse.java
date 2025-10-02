package bankapp.auth.application.shared.port.out.dto;

import bankapp.auth.application.verification.complete.CompleteVerificationResponse;

import java.util.UUID;

public record LoginResponse(
        PublicKeyCredentialRequestOptions options,
        UUID sessionId
) implements CompleteVerificationResponse {
}
