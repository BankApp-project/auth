package bankapp.auth.rest.shared.dto;

public record AuthenticationGrantResponse(
        String accessToken,
        String refreshToken
) {
}
