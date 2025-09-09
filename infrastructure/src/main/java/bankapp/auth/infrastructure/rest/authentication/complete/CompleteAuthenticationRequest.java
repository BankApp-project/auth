package bankapp.auth.infrastructure.rest.authentication.complete;

import java.util.UUID;

public record CompleteAuthenticationRequest(
        String challengeId,
        String AuthenticationResponseJSON,
        UUID credentialId
) {
}
