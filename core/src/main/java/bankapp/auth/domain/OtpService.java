package bankapp.auth.domain;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verification.complete.OtpVerificationException;
import bankapp.auth.application.verification.initiate.VerificationData;
import bankapp.auth.application.verification.initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;

import java.util.Optional;

public class OtpService {

    private final OtpGenerationPort otpGenerator;
    private final HashingPort hasher;
    private final OtpConfigPort config;
    private final OtpRepository otpRepository;

    public OtpService(
            @NotNull OtpGenerationPort otpGenerator,
            @NotNull HashingPort hasher,
            @NotNull OtpConfigPort config,
            @NotNull OtpRepository otpRepository
    ) {
        this.otpGenerator = otpGenerator;
        this.hasher = hasher;
        this.config = config;
        this.otpRepository = otpRepository;
    }
    public VerificationData createVerificationOtp(EmailAddress email) {
        String rawOtpCode = otpGenerator.generate(config.getOtpSize());

        String hashedOtpCode = hasher.hashSecurely(rawOtpCode);

        Otp otpToPersist = Otp.createNew(email.getValue(), hashedOtpCode, config.getClock(), config.getTtl());

        otpRepository.save(otpToPersist);

        return new VerificationData(rawOtpCode);
    }

    public void verifyAndConsumeOtp(EmailAddress email, String otpValue) {
        Optional<Otp> persistedOtpOptional = otpRepository.load(email.getValue());

        var persistedOtp = persistedOtpOptional
                .orElseThrow(() -> new OtpVerificationException("No such OTP in the system"));

        verifyOtp(otpValue, persistedOtp);

        otpRepository.delete(email.getValue());
    }

    private void verifyOtp(String otpValue, Otp otp) {
        if (!otp.isValid(config.getClock())) {
            throw new OtpVerificationException("Otp has expired");
        }
        if (!hasher.verify(otp.getValue(), otpValue)) {
            throw new OtpVerificationException("Otp does not match");
        }
    }

}
