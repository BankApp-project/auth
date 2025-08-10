package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.EventPublisher;
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;

public class InitiateVerificationUseCase {

    private static final int OTP_SIZE = 6;

    private final EventPublisher eventPublisher;
    private final OtpGeneratorPort otpGenerator;


    public InitiateVerificationUseCase(EventPublisher eventPublisher, OtpGeneratorPort otpGenerator) {
        this.eventPublisher = eventPublisher;
        this.otpGenerator = otpGenerator;
    }

    public Otp handle(InitiateVerificationCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        //generateSecureOtpCommand ???
        Otp otp = otpGenerator.generate(email.toString(),OTP_SIZE);

        eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(otp));
        return otp;
    }
}
