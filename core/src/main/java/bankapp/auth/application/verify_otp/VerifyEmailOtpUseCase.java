package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.CredentialOptionsPort;
import bankapp.auth.application.verify_otp.port.out.CredentialRepository;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.AuthSession;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;

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

        var persistedOtp = fetchPersistedOtp(email.getValue());
        verifyOtp(persistedOtp, command.value());
        otpRepository.delete(persistedOtp.getKey());

        var challenge = challengeGenerator.generate();
        UUID sessionId = UUID.randomUUID();

        Optional<User> userOptional = userRepository.findByEmail(command.key());
        User user;

        if (userOptional.isEmpty()) {
            user = new User(email);
            userRepository.save(user);
            saveSession(sessionId, challenge, user.getId());
            return new RegistrationResponse(credentialOptionsPort.getPasskeyCreationOptions(user, challenge), sessionId);
        }

        user = userOptional.get();
        saveSession(sessionId, challenge, user.getId());

        if (user.isEnabled()) {
            //if user enabled then has credential, so can log in
            var userCredentials = credentialRepository.load(user.getId());
            return new LoginResponse(credentialOptionsPort.getPasskeyRequestOptions(userCredentials, challenge), sessionId);
        } else {
            return new RegistrationResponse(credentialOptionsPort.getPasskeyCreationOptions(user, challenge), sessionId);
        }
    }

    private Otp fetchPersistedOtp(String key) {
        Optional<Otp> persistedOtpOptional = otpRepository.load(key);
        if (persistedOtpOptional.isEmpty()) {
            throw new VerifyEmailOtpException("No such OTP in the system");
        }
        return persistedOtpOptional.get();
    }

    private void saveSession(UUID ceremonyId, byte[] challenge, UUID userId) {
        AuthSession authSession = new AuthSession(
                ceremonyId,
                challenge,
                userId,
                Instant.now(clock)
        );
        sessionRepository.save(authSession, ceremonyId);
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