package bankapp.auth.application.verify_otp;

import bankapp.auth.domain.model.PublicKeyCredentialRequestOptions;

public record VerifyEmailOtpResponse(PublicKeyCredentialRequestOptions requestOptions) {
}
