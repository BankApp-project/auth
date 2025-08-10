package bankapp.auth.application.usecase;

import bankapp.auth.application.dto.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.EventPublisher;
import bankapp.auth.application.port.out.HashingPort;
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;

public class InitiateVerificationUseCase {

    private static final int OTP_SIZE = 6;

    private final EventPublisher eventPublisher;
    private final OtpGeneratorPort otpGenerator;
    private final HashingPort hasher;


    public InitiateVerificationUseCase(EventPublisher eventPublisher, OtpGeneratorPort otpGenerator, HashingPort hasher) {
        this.eventPublisher = eventPublisher;
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
    }

    public Otp handle(InitiateVerificationCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        //generateSecureOtpCommand ???
        Otp otp = otpGenerator.generate(email.toString(),OTP_SIZE);
        var hashedValue = hasher.hashSecurely(otp.getValue());
        Otp hashedOtp = new Otp(hashedValue, email.toString());

        eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(otp));
        return otp;
    }
}
