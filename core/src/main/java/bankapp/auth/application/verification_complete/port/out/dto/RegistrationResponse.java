package bankapp.auth.application.verification_complete.port.out.dto;

import bankapp.auth.application.verification_complete.CompleteVerificationResponse;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;

public record RegistrationResponse(
        PublicKeyCredentialCreationOptions options,
        java.util.UUID sessionId
) implements CompleteVerificationResponse {
}
