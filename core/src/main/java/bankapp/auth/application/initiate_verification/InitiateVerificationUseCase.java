package bankapp.auth.application.initiate_verification;

import bankapp.auth.application.initiate_verification.exception.InitiateVerificationException;
import bankapp.auth.application.initiate_verification.port.in.commands.InitiateVerificationCommand;
import bankapp.auth.application.initiate_verification.port.out.HasherPort;
import bankapp.auth.application.initiate_verification.port.out.OtpGeneratorPort;
import bankapp.auth.application.initiate_verification.port.out.OtpSaverPort;
import bankapp.auth.application.initiate_verification.port.out.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.shared.port.out.*;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.Nullable;

public class InitiateVerificationUseCase {

    private final int otpSize;

    private final EventPublisherPort eventPublisher;
    private final OtpGeneratorPort otpGenerator;
    private final HasherPort hasher;
    private final OtpSaverPort otpRepository;
    private final CommandBus commandBus;


    public InitiateVerificationUseCase(
            @NotNull EventPublisherPort eventPublisher,
            @NotNull OtpGeneratorPort otpGenerator,
            @NotNull HasherPort hasher,
            @NotNull OtpSaverPort otpRepository,
            @NotNull CommandBus commandBus,
            @Nullable Integer otpSize
            ) {
        this.eventPublisher = eventPublisher;
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
        this.otpRepository = otpRepository;
        this.commandBus = commandBus;

       this.otpSize = otpSize == null ? 6 : otpSize;
    }

    public Otp handle(InitiateVerificationCommand command) {

        try {
            Otp otp = otpGenerator.generate(command.email().toString(), otpSize);

            var hashedValue = hasher.hashSecurely(otp.getValue());

            Otp hashedOtp = new Otp(hashedValue, command.email().toString());

            otpRepository.save(hashedOtp);

            commandBus.sendOtpToUserEmail(otp.getKey(), otp.getValue());

            eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(hashedOtp));

            return otp;
        } catch (Exception e) {
            throw new InitiateVerificationException("Failed to initiate verification: " + e.getMessage(), e);
        }
    }
}
