package bankapp.auth.application.port.in.commands;

import bankapp.auth.domain.model.vo.EmailAddress;

public record InitiateVerificationCommand(EmailAddress email) {
}
