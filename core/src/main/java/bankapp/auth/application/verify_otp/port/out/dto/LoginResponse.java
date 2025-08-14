package bankapp.auth.application.verify_otp.port.out.dto;

import bankapp.auth.application.verify_otp.VerifyEmailOtpResponse;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

public record LoginResponse(PublicKeyCredentialRequestOptions options) implements VerifyEmailOtpResponse {
}
