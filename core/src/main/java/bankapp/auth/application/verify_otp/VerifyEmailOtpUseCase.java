package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.CredentialRepository;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.AuthSession;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.application.verify_otp.port.out.CredentialOptionsPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class VerifyEmailOtpUseCase {


    private final Clock clock;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;
    private final CredentialOptionsPort credentialOptionsPort;
    private final CredentialRepository credentialRepository;
    private final ChallengeGenerationPort challengeGenerator;
    private final SessionRepository sessionRepository;

    public VerifyEmailOtpUseCase(
            @NotNull Clock clock,
            @NotNull OtpRepository otpRepository,
            @NotNull HashingPort hasher,
            @NotNull UserRepository userRepository,
            @NotNull CredentialOptionsPort credentialOptionsPort,
            @NotNull CredentialRepository credentialRepository,
            @NotNull ChallengeGenerationPort challengeGenerator,
            @NotNull SessionRepository sessionRepository
    ) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
        this.credentialOptionsPort = credentialOptionsPort;
        this.credentialRepository = credentialRepository;
        this.challengeGenerator = challengeGenerator;
        this.sessionRepository = sessionRepository;
    }

    public VerifyEmailOtpResponse handle(VerifyEmailOtpCommand command) {
        EmailAddress email = command.key();
        String key = email.getValue();
        String value = command.value();

        Optional<Otp> persistedOtpOptional = otpRepository.load(key);
        if (persistedOtpOptional.isEmpty()) {
            throw new VerifyEmailOtpException("No such OTP in the system");
        }
        var persistedOtp = persistedOtpOptional.get();
        verifyOtp(persistedOtp, value);
        otpRepository.delete(persistedOtp.getKey());

        Optional<User> userOptional = userRepository.findByEmail(command.key());

        var challenge = challengeGenerator.generate();
        UUID sessionId = UUID.randomUUID();
  

        if (userOptional.isPresent() && userOptional.get().isEnabled()) {
            var userCredentials = credentialRepository.load(userOptional.get().getId());
            saveSession(sessionId, challenge, userOptional.get());
            return new LoginResponse(credentialOptionsPort.getPasskeyRequestOptions(userOptional.get(), userCredentials, challenge), sessionId.toString());
        } else {
            User user = new User(email);
            userRepository.save(user);
            saveSession(sessionId, challenge, user);
            return new RegistrationResponse(credentialOptionsPort.getPasskeyCreationOptions(user, challenge), sessionId.toString());
        }
    }

    private void saveSession(UUID ceremonyId, byte[] challenge, User user) {
        AuthSession authSession = new AuthSession(
                ceremonyId.toString(),
                challenge,
                user.getId(),
                Instant.now(clock)
                );
        sessionRepository.save(authSession, ceremonyId.toString());
    }


    private void verifyOtp(Otp persistedOtp, String value) {

        if (!persistedOtp.isValid(clock)) {
            throw new VerifyEmailOtpException("Otp has expired");
        }
        if (!hasher.verify(persistedOtp.getValue(), value)) {
            throw new VerifyEmailOtpException("Otp does not match");
        }
    }
}