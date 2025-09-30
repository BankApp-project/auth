package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;

public record EmailTemplate(
        String subject,
        String body,
        EmailAddress sendTo
) {
}
