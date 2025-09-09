package bankapp.auth.infrastructure.rest.verification.initiate;

import bankapp.auth.application.verification.initiate.InitiateVerificationUseCase;
import bankapp.auth.application.verification.initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@ConditionalOnProperty(
        name = "app.feature.verification.initiate.enabled",
        havingValue = "true"
)
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncInitiateVerificationService {

    private final InitiateVerificationUseCase initiateVerificationUseCase;

    @Async
    public void handle(InitiateVerificationCommand command) {
        try {
            initiateVerificationUseCase.handle(command);
        } catch (Exception e) {
            logEmailVerificationError(e, command.email());
        }
    }

    private void logEmailVerificationError(Throwable e, EmailAddress email) {
        log.error("Failed to process email verification for email: {}", email.getValue(), e);
    }
}