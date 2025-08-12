package bankapp.auth.application.initiate_verification.port.in.commands;

import bankapp.auth.domain.model.vo.EmailAddress;

public record InitiateVerificationCommand(EmailAddress email) {
}
