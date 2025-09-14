package bankapp.auth.infrastructure.driving.rest.authentication.initiate;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

public record InitiateAuthenticationResponse(
        PublicKeyCredentialRequestOptions loginOptions,
        String sessionId) {
}
