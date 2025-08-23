package bankapp.auth.application.verification_complete.port.out.dto;

import bankapp.auth.application.verification_complete.CompleteVerificationResponse;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;

import java.util.UUID;

public record RegistrationResponse(
        PublicKeyCredentialCreationOptions options,
        UUID challengeId
) implements CompleteVerificationResponse {
}
