package bankapp.auth.application.shared.port.out.dto;

public record AuthTokens(String accessToken, String refreshToken) {}
