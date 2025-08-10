package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.EventPublisher;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;

public class InitiateVerificationUseCase {

    private final EventPublisher eventPublisher;

    public InitiateVerificationUseCase(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    private static final int OTP_SIZE = 6;

    public void handle(InitiateVerificationCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        //generateSecureOtpCommand ???
        var otp = new Otp("123456", email.toString());

        eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(otp));
    }
}
