package bankapp.auth.rest.authentication;

public record CompleteAuthenticationRequest(
        String challengeId,
        String AuthenticationResponseJSON,
        byte[] credentialId
) {
}
