package bankapp.auth.infrastructure.rest.authentication;

public record CompleteAuthenticationRequest(
        String challengeId,
        String AuthenticationResponseJSON,
        byte[] credentialId
) {
}
