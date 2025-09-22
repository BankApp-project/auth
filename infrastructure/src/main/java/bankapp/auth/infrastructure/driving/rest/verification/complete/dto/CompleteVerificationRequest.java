package bankapp.auth.infrastructure.driving.rest.verification.complete.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// --- Request DTO ---
@Schema(description = "Request body to complete the email verification process using an OTP.")
public record CompleteVerificationRequest(
        @Schema(
                description = "The user's email address, which must match the one used to initiate verification.",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "The One-Time Password (OTP) received by the user via email.",
                example = "123456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String otpValue
) {
}
