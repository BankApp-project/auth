package bankapp.auth.infrastructure.driving.rest.authentication.complete;

import java.util.UUID;

public record CompleteAuthenticationRequest(
        String challengeId,
        String AuthenticationResponseJSON,
        UUID credentialId
) {
}
