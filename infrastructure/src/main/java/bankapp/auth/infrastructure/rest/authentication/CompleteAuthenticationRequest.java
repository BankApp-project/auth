package bankapp.auth.infrastructure.rest.authentication;

import java.util.UUID;

public record CompleteAuthenticationRequest(
        String challengeId,
        String AuthenticationResponseJSON,
        UUID credentialId
) {
}
