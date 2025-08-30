package bankapp.auth.infrastructure.rest.shared.dto;

public record AuthenticationGrantResponse(
        String accessToken,
        String refreshToken
) {
}
