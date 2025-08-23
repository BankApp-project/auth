package bankapp.auth.application.shared.port.out.dto;

public record AuthenticationGrant(
        AuthTokens authTokens
) {
}