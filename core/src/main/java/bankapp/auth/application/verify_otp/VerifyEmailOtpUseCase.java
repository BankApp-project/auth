package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Otp;

public class VerifyEmailOtpUseCase {

    OtpRepository otpRepository;

    public VerifyEmailOtpUseCase(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public boolean handle(VerifyEmailOtpCommand command) {
        Otp otp = command.otp();
        Otp persistedOtp = otpRepository.load(command.otp().getKey());
        return persistedOtp != null && persistedOtp.isValid() && otp.equals(persistedOtp);
    }
}
