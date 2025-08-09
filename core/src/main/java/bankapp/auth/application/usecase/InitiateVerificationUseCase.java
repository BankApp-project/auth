package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.EventPublisher;
import bankapp.auth.domain.model.Otp;

public class InitiateVerificationUseCase {

    private final EventPublisher eventPublisher;

    public InitiateVerificationUseCase(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void handle(InitiateVerificationCommand command) {
        var otp = new Otp("123456", command.email());

        eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(otp));
    }
}
