package bankapp.auth.application.initiate_verification;

import bankapp.auth.application.initiate_verification.exception.InitiateVerificationException;
import bankapp.auth.application.initiate_verification.port.in.commands.InitiateVerificationCommand;
import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.initiate_verification.port.out.OtpGenerationPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.initiate_verification.port.out.events.EmailVerificationOtpGeneratedEvent;
import bankapp.auth.application.shared.port.out.*;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.Nullable;

import java.time.Instant;

public class InitiateVerificationUseCase {

    private final int otpSize;
    private final int ttl;

    private final EventPublisherPort eventPublisher;
    private final OtpGenerationPort otpGenerator;
    private final HashingPort hasher;
    private final OtpRepository otpRepository;
    private final NotificationPort notificator;


    public InitiateVerificationUseCase(
            @NotNull EventPublisherPort eventPublisher,
            @NotNull OtpGenerationPort otpGenerator,
            @NotNull HashingPort hasher,
            @NotNull OtpRepository otpRepository,
            @NotNull NotificationPort notificator,
            @Nullable Integer otpSize,
            @Nullable Integer defaultTtl) {
        this.eventPublisher = eventPublisher;
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
        this.otpRepository = otpRepository;
        this.notificator = notificator;

       this.otpSize = otpSize == null ? 6 : otpSize;
       this.ttl = defaultTtl == null ? 10 : defaultTtl;
    }

    public Otp handle(InitiateVerificationCommand command) {

        try {
            String otpValue = otpGenerator.generate(otpSize);

            Otp otp = new Otp(otpValue, command.email().toString());

            var hashedValue = hasher.hashSecurely(otpValue);

            Otp hashedOtp = new Otp(hashedValue, command.email().toString());

            hashedOtp.setExpirationTime(getTtlInSeconds());

            otpRepository.save(hashedOtp);

            notificator.sendOtpToUserEmail(otp.getKey(), otp.getValue());

            eventPublisher.publish(new EmailVerificationOtpGeneratedEvent(hashedOtp));

            return otp;
        } catch (Exception e) {
            throw new InitiateVerificationException("Failed to initiate verification: " + e.getMessage(), e);
        }
    }

    private int getTtlInSeconds() {
        return ttl * 60;
    }
}
