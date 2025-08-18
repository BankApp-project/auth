package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.CredentialRepository;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.CredentialOptionsService;
import bankapp.auth.domain.service.UserService;

import java.time.Clock;
import java.util.*;

public class VerifyEmailOtpUseCase {


    private final Clock clock;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CredentialOptionsService credentialOptionsService;
    private final CredentialRepository credentialRepository;
    private final ChallengeGenerationPort challengeGenerator;

    public VerifyEmailOtpUseCase(
            Clock clock,
            OtpRepository otpRepository,
            HashingPort hasher,
            UserRepository userRepository,
            UserService userService,
            CredentialOptionsService credentialOptionsService,
            CredentialRepository credentialRepository, ChallengeGenerationPort challengeGenerator) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
        this.userService = userService;
        this.credentialOptionsService = credentialOptionsService;
        this.credentialRepository = credentialRepository;
        this.challengeGenerator = challengeGenerator;
    }

    public VerifyEmailOtpResponse handle(VerifyEmailOtpCommand command) {
        EmailAddress email = command.key();
        String key = email.getValue();
        String value = command.value();

        Otp persistedOtp = otpRepository.load(key);
        verifyOtp(persistedOtp, value);

        Optional<User> userOpt = userRepository.findByEmail(command.key());

        var challenge = challengeGenerator.generate();

        if (userOpt.isPresent() && userOpt.get().isEnabled()) {
            var userCredentials = credentialRepository.load(userOpt.get().getId());
            return new LoginResponse(credentialOptionsService.getPasskeyRequestOptions(userOpt.get(), userCredentials, challenge));
        } else {
            User user = userService.createUser(email);
            userRepository.save(user);
            return new RegistrationResponse(credentialOptionsService.getPasskeyCreationOptions(user));
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