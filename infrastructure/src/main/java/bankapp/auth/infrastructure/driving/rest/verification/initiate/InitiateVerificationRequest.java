package bankapp.auth.infrastructure.driving.rest.verification.initiate;

import jakarta.validation.constraints.Email;

public record InitiateVerificationRequest(
        @Email
        String email
) {
}
