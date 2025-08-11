package bankapp.auth.application.usecase;

import bankapp.auth.application.port.in.commands.InitiateVerificationCommand;
import bankapp.auth.application.dto.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.port.out.EventPublisherPort;
import bankapp.auth.application.port.out.HasherPort;
import bankapp.auth.application.port.out.OtpGeneratorPort;
import bankapp.auth.application.port.out.OtpSaverPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.Nullable;

public class InitiateVerificationUseCase {

    private final int otpSize;

    private final EventPublisherPort eventPublisher;
    private final OtpGeneratorPort otpGenerator;
    private final HasherPort hasher;
    private final OtpSaverPort otpRepository;


    public InitiateVerificationUseCase(
            @NotNull EventPublisherPort eventPublisher,
            @NotNull OtpGeneratorPort otpGenerator,
            @NotNull HasherPort hasher,
            @NotNull OtpSaverPort otpRepository,
            @Nullable Integer otpSize
            ) {
        this.eventPublisher = eventPublisher;
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
        this.otpRepository = otpRepository;

       this.otpSize = otpSize == null ? 6 : otpSize;
    }

    public Otp handle(InitiateVerificationCommand command) {

        Otp otp = otpGenerator.generate(command.email().toString(), otpSize);

        var hashedValue = hasher.hashSecurely(otp.getValue());

        Otp hashedOtp = new Otp(hashedValue, command.email().toString());

        otpRepository.save(hashedOtp);

        eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(otp));

        return otp;
    }
}
