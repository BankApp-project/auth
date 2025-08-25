package bankapp.auth.rest.authentication;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

public record InitiateAuthenticationResponse(
        PublicKeyCredentialRequestOptions options,
        String ChallengeId) {
}
