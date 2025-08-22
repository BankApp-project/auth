package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.LoggerPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
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

    private final long sessionTtl;

    private final LoggerPort log;
    private final Clock clock;

    private final OtpRepository otpRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    private final CredentialOptionsPort credentialOptionsPort;
    private final ChallengeGenerationPort challengeGenerator;
    private final HashingPort hasher;

    public CompleteVerificationUseCase(
            long sessionTtl,
            @NotNull LoggerPort log,
            @NotNull Clock clock,
            @NotNull OtpRepository otpRepository,
            @NotNull SessionRepository sessionRepository,
            @NotNull CredentialRepository credentialRepository,
            @NotNull UserRepository userRepository,
            @NotNull CredentialOptionsPort credentialOptionsPort,
            @NotNull ChallengeGenerationPort challengeGenerator,
            @NotNull HashingPort hasher
    ) {
        this.sessionTtl = sessionTtl;
        this.log = log;
        this.clock = clock;
        this.otpRepository = otpRepository;
        this.sessionRepository = sessionRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.credentialOptionsPort = credentialOptionsPort;
        this.challengeGenerator = challengeGenerator;
        this.hasher = hasher;
    }

    //TODO DIVIDE IT TO VERIFICATION_COMPLETE AND REGISTRATION_INITIATE / AUTHENTICATION_INITIATE
    public CompleteVerificationResponse handle(CompleteVerificationCommand command) {
        log.info("Starting verification completion for email: {}", command.key().getValue());

        verifyAndConsumeOtp(command.key(), command.value());
        log.debug("OTP verified and consumed successfully");

        User user = findOrCreateUser(command.key());
        log.debug("User found/created with ID: {}", user.getId());

        byte[] challenge = challengeGenerator.generate();
        log.debug("challenge generated");

        var session = saveSession(challenge, user.getId());
        log.debug("Session saved successfully");

        CompleteVerificationResponse response = prepareResponse(user, challenge, session.sessionId());
        log.info("Verification completion successful for user: {}, response type: {}",
                user.getId(), response.getClass().getSimpleName());

        return response;
    }


    private void verifyAndConsumeOtp(EmailAddress email, String otpValue) {
        Optional<Otp> persistedOtpOptional = otpRepository.load(email.getValue());
        if (persistedOtpOptional.isEmpty()) {
            throw new CompleteVerificationException("No such OTP in the system");
        }

        var persistedOtp = persistedOtpOptional.get();

        if (!persistedOtp.isValid(clock)) {
            throw new CompleteVerificationException("Otp has expired");
        }
        if (!hasher.verify(persistedOtp.getValue(), otpValue)) {
            throw new CompleteVerificationException("Otp does not match");
        }

        otpRepository.delete(persistedOtp.getKey());
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

    private Challenge saveSession(byte[] challenge, UUID userId) {
        try {
            UUID sessionId = UUID.randomUUID();
            Challenge registrationSession = new Challenge(
                    sessionId,
                    challenge,
                    userId,
                    sessionTtl,
                    clock
            );
            sessionRepository.save(registrationSession, sessionId);
            return registrationSession;
        } catch (RuntimeException e) {
            throw new CompleteVerificationException("Failed to save session", e);
        }
    }

    private CompleteVerificationResponse prepareResponse(User user, byte[] challenge, UUID sessionId) {
        try {
            if (user.isEnabled()) {
                var userCredentials = credentialRepository.loadForUserId(user.getId());
                var passkeyOptions = credentialOptionsPort.getPasskeyRequestOptions(userCredentials, challenge);
                return new LoginResponse(passkeyOptions, sessionId);
            }
            var passkeyOptions = credentialOptionsPort.getPasskeyCreationOptions(user,challenge);
            return new RegistrationResponse(passkeyOptions, sessionId);
        } catch (RuntimeException e) {
            throw new CompleteVerificationException("Failed to prepare response", e);
        }
    }
}