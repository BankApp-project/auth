package bankapp.auth.application.dto.commands;

import bankapp.auth.domain.model.vo.EmailAddress;

public record InitiateVerificationCommand(EmailAddress email) {
}
