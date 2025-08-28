package bankapp.auth.domain;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.verification_initiate.VerificationData;
import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;

public class OtpService {

    private final OtpGenerationPort otpGenerator;
    private final HashingPort hasher;
    private final OtpConfigPort config;

    public OtpService(
            @NotNull OtpGenerationPort otpGenerator,
            @NotNull HashingPort hasher,
            @NotNull OtpConfigPort config
    ) {
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
        this.config = config;
    }
    public VerificationData createVerificationOtp(EmailAddress email) {
        String rawOtpCode = otpGenerator.generate(config.getOtpSize());

        String hashedOtpCode = hasher.hashSecurely(rawOtpCode);

        Otp otpToPersist = Otp.createNew(email.getValue(), hashedOtpCode, config.getClock(), config.getTtl().getSeconds());

        return new VerificationData(otpToPersist, rawOtpCode);
    }
}
