package bankapp.auth.application.verify_otp;

import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;

public record RegistrationResponse(PublicKeyCredentialCreationOptions options) implements VerifyEmailOtpResponse {
}
