package bankapp.auth.application.authentication_complete;

import java.util.UUID;

public record CompleteAuthenticationCommand(
    UUID challengeId,
    String AuthenticationResponseJSON,
    UUID credentialId
) {
    public CompleteAuthenticationCommand {
        if (AuthenticationResponseJSON == null || AuthenticationResponseJSON.isBlank()) {
            throw new IllegalArgumentException("Authentication Response JSON cannot be null or blank");
        }
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge ID cannot be null");
        }
        if (credentialId == null) {
            throw new IllegalArgumentException("Credential ID cannot be null");
        }
    }
}
