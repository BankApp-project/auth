package bankapp.auth.application.verification.complete.port.out.dto;

import bankapp.auth.application.verification.complete.CompleteVerificationResponse;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;

import java.util.UUID;

public record RegistrationResponse(
        PublicKeyCredentialCreationOptions options,
        UUID challengeId
) implements CompleteVerificationResponse {
}
