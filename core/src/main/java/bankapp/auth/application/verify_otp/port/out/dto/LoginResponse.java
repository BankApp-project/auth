package bankapp.auth.application.verify_otp.port.out.dto;

import bankapp.auth.application.verify_otp.VerifyEmailOtpResponse;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

import java.util.UUID;

public record LoginResponse(
        PublicKeyCredentialRequestOptions options,
        UUID sessionId
) implements VerifyEmailOtpResponse {
}
