package bankapp.auth.application.verification_initiate.port.in;

import bankapp.auth.domain.model.vo.EmailAddress;

public record InitiateVerificationCommand(EmailAddress email) {
}
