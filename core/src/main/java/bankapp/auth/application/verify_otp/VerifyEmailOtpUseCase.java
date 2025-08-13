package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Challenge;
import bankapp.auth.domain.model.Otp;

import java.time.Clock;

public class VerifyEmailOtpUseCase {

    OtpRepository otpRepository;
    HashingPort hasher;

    Clock clock;

    public VerifyEmailOtpUseCase(Clock clock, OtpRepository otpRepository, HashingPort hasher) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
    }

    public Challenge handle(VerifyEmailOtpCommand command) {
        String key = command.key().getValue();
        String value = command.value();
        Otp persistedOtp = otpRepository.load(key);

        verifyOtp(persistedOtp, value);

         return new Challenge();
    }

    private void verifyOtp(Otp persistedOtp, String value) {
        if (persistedOtp == null) {
            throw new VerifyEmailOtpException("No such OTP in the system");
        }
        if (!persistedOtp.isValid(clock)) {
            throw new VerifyEmailOtpException("Otp has expired");
        }
        if (!hasher.verify(persistedOtp.getValue(), value)) {
            throw new VerifyEmailOtpException("Otp does not match");
        }
    }
}
