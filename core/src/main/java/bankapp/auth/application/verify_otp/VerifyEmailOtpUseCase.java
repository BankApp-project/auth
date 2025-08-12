package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;

public class VerifyEmailOtpUseCase {

    OtpRepository otpRepository;

    public VerifyEmailOtpUseCase(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public boolean handle(VerifyEmailOtpCommand command) {
        var otp = otpRepository.load(command.otp().getKey());
        return otp != null;
    }
}
