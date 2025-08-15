package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.ByteArrayUtil;
import bankapp.auth.domain.service.UserService;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VerifyEmailOtpUseCase {

    private final String rpId;
    private final Clock clock;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ChallengeGenerationPort challengeGenerator;

    public VerifyEmailOtpUseCase(
            String rpId, Clock clock,
            OtpRepository otpRepository,
            HashingPort hasher,
            UserRepository userRepository,
            UserService userService,
            ChallengeGenerationPort challengeGenerator) {

        this.rpId = rpId;
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
        this.userService = userService;
        this.challengeGenerator = challengeGenerator;
    }

    public VerifyEmailOtpResponse handle(VerifyEmailOtpCommand command) {
        EmailAddress email = command.key();
        String key = email.getValue();
        String value = command.value();

        Otp persistedOtp = otpRepository.load(key);
        verifyOtp(persistedOtp, value);

        Optional<User> userOpt = userRepository.findByEmail(command.key());
        if (userOpt.isEmpty()) {
            User user = userService.createUser(email);
            userRepository.save(user);
            return getRegistrationResponse(user);
        } else {
            return getLoginResponse();
        }
    }

    private LoginResponse getLoginResponse() {
        byte[] challenge = challengeGenerator.generate();
        return new LoginResponse(new PublicKeyCredentialRequestOptions(challenge, null, null, null, null, null));
    }

    private RegistrationResponse getRegistrationResponse(User user) {
        String name = user.getEmail().getValue();
        UUID userId = user.getId();

        byte[] challenge = challengeGenerator.generate();
        byte[] userHandle = ByteArrayUtil.uuidToBytes(userId);

        var userEntity = new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(userHandle, name, name);
        var rp = new PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity(rpId, rpId);

        var pubKeyCredParamES256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key", -7);
        var pubKeyCredParamRS256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key",-257);

        var pubKeyCredParamList = List.of(pubKeyCredParamES256, pubKeyCredParamRS256);

        return new RegistrationResponse(new PublicKeyCredentialCreationOptions(rp, userEntity, challenge, pubKeyCredParamList, null, null, null, null, null, null, null));
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
