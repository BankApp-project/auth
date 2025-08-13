package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Challenge;
import bankapp.auth.domain.model.Otp;

import java.time.Clock;

public class VerifyEmailOtpUseCase {

    OtpRepository otpRepository;

    Clock clock;

    public VerifyEmailOtpUseCase(Clock clock, OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
        this.clock = clock;
    }

    public Challenge handle(VerifyEmailOtpCommand command) {
        Otp otp = command.otp();
        Otp persistedOtp = otpRepository.load(command.otp().getKey());

        verifyPersistedOtp(persistedOtp, otp);

         return new Challenge();
    }

    private void verifyPersistedOtp(Otp persistedOtp, Otp otp) {
        if (persistedOtp == null) {
            throw new VerifyEmailOtpException("No such OTP in the system");
        }
        if (!persistedOtp.isValid(clock)) {
            throw new VerifyEmailOtpException("Otp has expired");
        }
        if (!otp.equals(persistedOtp)) {
            throw new VerifyEmailOtpException("Otp does not match");
        }
    }
}
