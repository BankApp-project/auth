package bankapp.auth.application.verify_otp.port.out.dto;

import bankapp.auth.application.verify_otp.VerifyEmailOtpResponse;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;

public record RegistrationResponse(PublicKeyCredentialCreationOptions options) implements VerifyEmailOtpResponse {
}
