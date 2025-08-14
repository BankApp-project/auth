package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.time.Clock;
import java.util.Optional;

public class VerifyEmailOtpUseCase {

    private final Clock clock;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;

    public VerifyEmailOtpUseCase(Clock clock, OtpRepository otpRepository, HashingPort hasher, UserRepository userRepository) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
    }

    public VerifyEmailOtpResponse handle(VerifyEmailOtpCommand command) {
        EmailAddress email = command.key();
        String key = email.getValue();
        String value = command.value();

        Otp persistedOtp = otpRepository.load(key);
        verifyOtp(persistedOtp, value);

        Optional<User> userOpt = userRepository.findByEmail(command.key());
        if (userOpt.isEmpty()) {
            User user = new User(email);
            userRepository.save(user);
            return getRegistrationResponse();
        } else {
            return getLoginResponse();
        }
    }

    private LoginResponse getLoginResponse() {
        return new LoginResponse(new PublicKeyCredentialRequestOptions(null, null, null, null, null, null));
    }

    private RegistrationResponse getRegistrationResponse() {
        return new RegistrationResponse(new PublicKeyCredentialCreationOptions(null, null, null, null, null, null, null, null, null, null, null));
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
