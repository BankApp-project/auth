package bankapp.auth.rest.registration;

public record AuthenticationGrantResponse(
        String accessToken,
        String refreshToken
) {
}
