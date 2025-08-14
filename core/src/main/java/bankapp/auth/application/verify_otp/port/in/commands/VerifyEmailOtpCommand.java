package bankapp.auth.application.verify_otp.port.in.commands;

import bankapp.auth.domain.model.vo.EmailAddress;

public record VerifyEmailOtpCommand(EmailAddress key, String value) {
}