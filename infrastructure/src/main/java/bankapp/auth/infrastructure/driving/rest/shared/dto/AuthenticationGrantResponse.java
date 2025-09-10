package bankapp.auth.infrastructure.driving.rest.shared.dto;

public record AuthenticationGrantResponse(
        String accessToken,
        String refreshToken
) {
}
