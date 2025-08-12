package bankapp.auth.application.verify_otp.in.commands;

import bankapp.auth.domain.model.Otp;

public record VerifyEmailOtpCommand(Otp otp) {
}
