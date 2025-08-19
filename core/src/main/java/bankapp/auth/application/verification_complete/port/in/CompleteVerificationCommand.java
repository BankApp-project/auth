package bankapp.auth.application.verification_complete.port.in;

import bankapp.auth.domain.model.vo.EmailAddress;

public record CompleteVerificationCommand(EmailAddress key, String value) {
}