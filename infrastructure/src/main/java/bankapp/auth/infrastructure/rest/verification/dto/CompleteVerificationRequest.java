package bankapp.auth.infrastructure.rest.verification.dto;

public record CompleteVerificationRequest(String email, String value) {
}
