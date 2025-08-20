package bankapp.auth.application.registration_complete;

import java.util.UUID;

public record CompleteRegistrationCommand(
        UUID sessionId,
        String publicKeyCredentialJson
) {
    public CompleteRegistrationCommand {
        if (publicKeyCredentialJson == null || publicKeyCredentialJson.isBlank()) {
            throw new IllegalArgumentException("Public key credential JSON cannot be null or blank");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
    }
}