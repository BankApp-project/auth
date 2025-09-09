package bankapp.auth.infrastructure.rest.authentication;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

public record InitiateAuthenticationResponse(
        PublicKeyCredentialRequestOptions loginOptions,
        String challengeId) {
}
