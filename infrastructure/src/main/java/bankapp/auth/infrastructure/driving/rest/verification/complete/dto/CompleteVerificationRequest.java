package bankapp.auth.infrastructure.driving.rest.verification.complete.dto;

import jakarta.validation.constraints.Email;

public record CompleteVerificationRequest(
        @Email
        String email,
        String otpValue) {
}
