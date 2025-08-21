package bankapp.auth.application.verification_initiate;

import bankapp.auth.domain.model.Otp;

public record VerificationData(Otp otpToPersist, String rawOtpCode) {
}
