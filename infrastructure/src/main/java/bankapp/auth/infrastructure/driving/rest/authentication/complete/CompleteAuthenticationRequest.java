package bankapp.auth.infrastructure.driving.rest.authentication.complete;

import java.util.UUID;

public record CompleteAuthenticationRequest(
        String sessionId,
        String AuthenticationResponseJSON,
        UUID credentialId
) {
}
