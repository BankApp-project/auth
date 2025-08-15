package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.UserService;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

public class VerifyEmailOtpUseCase {

    private final Clock clock;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;
    private final UserService userService;

    public VerifyEmailOtpUseCase(Clock clock, OtpRepository otpRepository, HashingPort hasher, UserRepository userRepository, UserService userService) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
        this.userService = userService;
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
            return getRegistrationResponse(user.getId());
        } else {
            return getLoginResponse();
        }
    }

    private LoginResponse getLoginResponse() {
        return new LoginResponse(new PublicKeyCredentialRequestOptions(null, null, null, null, null, null));
    }

    private RegistrationResponse getRegistrationResponse(UUID userId) {
        byte[] challenge = uuidToBytes(UUID.randomUUID());
        byte[] userHandle = uuidToBytes(userId);
        var user = new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(userHandle,null,null);
        return new RegistrationResponse(new PublicKeyCredentialCreationOptions(null, user, challenge, null, null, null, null, null, null, null, null));
    }


    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
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
