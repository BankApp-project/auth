package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification_complete.port.out.CredentialRepository;
import bankapp.auth.application.verification_complete.port.out.UserRepository;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.application.verification_complete.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

public class CompleteVerificationUseCase {


    private final Clock clock;
    private final long sessionTtl;

    private final OtpRepository otpRepository;
    private final HashingPort hasher;
    private final UserRepository userRepository;
    private final CredentialOptionsPort credentialOptionsPort;
    private final CredentialRepository credentialRepository;
    private final ChallengeGenerationPort challengeGenerator;
    private final SessionRepository sessionRepository;

    public CompleteVerificationUseCase(
            @NotNull Clock clock,
            @NotNull OtpRepository otpRepository,
            @NotNull HashingPort hasher,
            @NotNull UserRepository userRepository,
            @NotNull CredentialOptionsPort credentialOptionsPort,
            @NotNull CredentialRepository credentialRepository,
            @NotNull ChallengeGenerationPort challengeGenerator,
            @NotNull SessionRepository sessionRepository,
            long sessionTtl) {
        this.otpRepository = otpRepository;
        this.clock = clock;
        this.hasher = hasher;
        this.userRepository = userRepository;
        this.credentialOptionsPort = credentialOptionsPort;
        this.credentialRepository = credentialRepository;
        this.challengeGenerator = challengeGenerator;
        this.sessionRepository = sessionRepository;
        this.sessionTtl = sessionTtl;
    }

    public CompleteVerificationResponse handle(CompleteVerificationCommand command) {
        verifyAndConsumeOtp(command.key(), command.value());

        User user = findOrCreateUser(command.key());

        byte[] challenge = challengeGenerator.generate();
        UUID sessionId = UUID.randomUUID();
        saveSession(sessionId, challenge, user.getId(), sessionTtl);

        return prepareResponse(user, challenge, sessionId);
    }

    private void verifyAndConsumeOtp(EmailAddress email, String otpValue) {
        Optional<Otp> persistedOtpOptional = otpRepository.load(email.getValue());
        if (persistedOtpOptional.isEmpty()) {
            throw new CompleteVerificationException("No such OTP in the system");
        }
        var persistedOtp = persistedOtpOptional.get();
        verifyOtp(persistedOtp, otpValue);
        otpRepository.delete(persistedOtp.getKey());
    }

    private void verifyOtp(Otp persistedOtp, String value) {
        if (!persistedOtp.isValid(clock)) {
            throw new CompleteVerificationException("Otp has expired");
        }
        if (!hasher.verify(persistedOtp.getValue(), value)) {
            throw new CompleteVerificationException("Otp does not match");
        }
    }

    private User findOrCreateUser(EmailAddress email) {
        var userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isEmpty()) {
            user = new User(email);
            userRepository.save(user);
            return user;
        }
        return userOptional.get();
    }

    private void saveSession(UUID sessionId, byte[] challenge, UUID userId, long ttl) {
        AuthSession authSession = new AuthSession(
                sessionId,
                challenge,
                userId,
                ttl,
                clock
        );
        sessionRepository.save(authSession, sessionId);
    }

    private CompleteVerificationResponse prepareResponse(User user, byte[] challenge, UUID sessionId) {
        if (user.isEnabled()) {
            var userCredentials = credentialRepository.load(user.getId());
            var passkeyOptions = credentialOptionsPort.getPasskeyRequestOptions(userCredentials, challenge);
            return new LoginResponse(passkeyOptions, sessionId);
        }
        var passkeyOptions = credentialOptionsPort.getPasskeyCreationOptions(user,challenge);
        return new RegistrationResponse(passkeyOptions, sessionId);
    }
}