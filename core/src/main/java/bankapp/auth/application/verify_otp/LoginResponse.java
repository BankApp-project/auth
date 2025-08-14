package bankapp.auth.application.verify_otp;

import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

public record LoginResponse(PublicKeyCredentialRequestOptions options) implements VerifyEmailOtpResponse{
}
