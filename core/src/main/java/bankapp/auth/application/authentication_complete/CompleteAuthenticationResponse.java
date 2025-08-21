package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;

public record CompleteAuthenticationResponse(AuthTokens authTokens) {
}
