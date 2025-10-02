package bankapp.auth.application.shared.port.out.dto;

import bankapp.auth.application.verification.complete.CompleteVerificationResponse;

import java.util.UUID;

public record RegistrationResponse(
        PublicKeyCredentialCreationOptions options,
        UUID sessionId
) implements CompleteVerificationResponse {
}
