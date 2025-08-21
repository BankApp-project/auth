package bankapp.auth.domain;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.verification_initiate.VerificationData;
import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class OtpService {

    private final OtpGenerationPort otpGenerator;
    private final HashingPort hasher;
    private final Clock clock;
    private final OtpConfigPort config;

    public OtpService(
            @NotNull OtpGenerationPort otpGenerator,
            @NotNull HashingPort hasher,
            @NotNull Clock clock,
            @NotNull OtpConfigPort config
    ) {
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
        this.clock = clock;
        this.config = config;
    }
    public VerificationData createVerificationOtp(EmailAddress email) {
        String rawOtpCode = otpGenerator.generate(config.getOtpSize());

        String hashedOtpCode = hasher.hashSecurely(rawOtpCode);

        Instant expirationTime = clock.instant().plus(config.getTtlInMinutes(), ChronoUnit.MINUTES);

        Otp otpToPersist = new Otp(email.getValue(), hashedOtpCode, expirationTime);

        return new VerificationData(otpToPersist, rawOtpCode);
    }
}
