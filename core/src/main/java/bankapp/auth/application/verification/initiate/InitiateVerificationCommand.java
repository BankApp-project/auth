package bankapp.auth.application.verification.initiate;

import bankapp.auth.domain.model.vo.EmailAddress;

public record InitiateVerificationCommand(EmailAddress email) {
}
