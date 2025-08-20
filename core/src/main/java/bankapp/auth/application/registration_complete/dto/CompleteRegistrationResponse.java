package bankapp.auth.application.registration_complete.dto;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;

public record CompleteRegistrationResponse(
        AuthTokens tokens
) {
}