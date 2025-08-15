package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.PasskeyOptionsService;
import bankapp.auth.domain.service.UserService;

import java.time.Clock;
import java.util.*;

public class VerifyEmailOtpUseCase {


    private final Clock clock;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasskeyOptionsService passkeyOptionsService;

    public VerifyEmailOtpUseCase(
            Clock clock,
            OtpRepository otpRepository,
            HashingPort hasher,
            UserRepository userRepository,
            UserService userService,
            PasskeyOptionsService passkeyOptionsService
    ) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passkeyOptionsService = passkeyOptionsService;
    }

    public VerifyEmailOtpResponse handle(VerifyEmailOtpCommand command) {
        EmailAddress email = command.key();
        String key = email.getValue();
        String value = command.value();

        Otp persistedOtp = otpRepository.load(key);
        verifyOtp(persistedOtp, value);

        Optional<User> userOpt = userRepository.findByEmail(command.key());

        if (userOpt.isPresent() && userOpt.get().isEnabled()) {
            return passkeyOptionsService.getLoginResponse();
        } else {
            User user = userService.createUser(email);
            userRepository.save(user);
            return passkeyOptionsService.getRegistrationResponse(user);
        }
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