package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.LoggerPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
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

public class CompleteVerificationUseCase {

    private final long challengeTtl;

    private final LoggerPort log;
    private final Clock clock;

    private final OtpRepository otpRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    private final CredentialOptionsPort credentialOptionsPort;
    private final ChallengeGenerationPort challengeGenerator;
    private final HashingPort hasher;

    public CompleteVerificationUseCase(
            long challengeTtl,
            @NotNull LoggerPort log,
            @NotNull Clock clock,
            @NotNull OtpRepository otpRepository,
            @NotNull ChallengeRepository challengeRepository,
            @NotNull CredentialRepository credentialRepository,
            @NotNull UserRepository userRepository,
            @NotNull CredentialOptionsPort credentialOptionsPort,
            @NotNull ChallengeGenerationPort challengeGenerator,
            @NotNull HashingPort hasher
    ) {
        this.challengeTtl = challengeTtl;
        this.log = log;
        this.clock = clock;
        this.otpRepository = otpRepository;
        this.challengeRepository = challengeRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.credentialOptionsPort = credentialOptionsPort;
        this.challengeGenerator = challengeGenerator;
        this.hasher = hasher;
    }

    //TODO THINK ABOUT DIVIDING IT TO VERIFICATION_COMPLETE AND REGISTRATION_INITIATE / AUTHENTICATION_INITIATE
    public CompleteVerificationResponse handle(CompleteVerificationCommand command) {
        log.info("Starting verification completion for email: {}", command.key().getValue());

        verifyAndConsumeOtp(command.key(), command.value());
        log.debug("OTP verified and consumed successfully");

        User user = findOrCreateUser(command.key());
        log.debug("User found/created with ID: {}", user.getId());

        var challenge = challengeGenerator.generate(clock, challengeTtl);
        log.debug("challenge generated");

        saveChallenge(challenge);
        log.debug("Challenge saved successfully");

        CompleteVerificationResponse response = prepareResponse(user, challenge);
        log.info("Verification completion successful for user: {}, response type: {}",
                user.getId(), response.getClass().getSimpleName());

        return response;
    }

    private void verifyAndConsumeOtp(EmailAddress email, String otpValue) {
        Optional<Otp> persistedOtpOptional = otpRepository.load(email.getValue());

        var persistedOtp = persistedOtpOptional
                .orElseThrow(() -> new CompleteVerificationException("No such OTP in the system"));

        verifyOtp(otpValue, persistedOtp);

        otpRepository.delete(email.getValue());
    }

    private void verifyOtp(String otpValue, Otp persistedOtp) {
        if (!persistedOtp.isValid(clock)) {
            throw new CompleteVerificationException("Otp has expired");
        }
        if (!hasher.verify(persistedOtp.getValue(), otpValue)) {
            throw new CompleteVerificationException("Otp does not match");
        }
    }

    private User findOrCreateUser(EmailAddress email) {
        var userOptional = userRepository.findByEmail(email);
        return userOptional.orElseGet(() -> createAndSaveUser(email));
    }

    private User createAndSaveUser(EmailAddress email) {
        User user = new User(email);
        userRepository.save(user);
        return user;
    }

    private void saveChallenge(Challenge challenge) {
        try {
            challengeRepository.save(challenge);
        } catch (RuntimeException e) {
            throw new CompleteVerificationException("Failed to save session", e);
        }
    }

    private CompleteVerificationResponse prepareResponse(User user, Challenge challenge) {
        try {
            if (user.isEnabled()) {
                return getLoginResponse(user, challenge);
            } else {
                return getRegistrationResponse(user, challenge);
            }
        } catch (RuntimeException e) {
            throw new CompleteVerificationException("Failed to prepare response", e);
        }
    }

    private LoginResponse getLoginResponse(User user, Challenge challenge) {
        var userCredentials = credentialRepository.loadForUserId(user.getId());
        var passkeyOptions = credentialOptionsPort.getPasskeyRequestOptions(userCredentials, challenge);
        return new LoginResponse(passkeyOptions, challenge.sessionId());
    }

    private RegistrationResponse getRegistrationResponse(User user, Challenge challenge) {
        var passkeyOptions = credentialOptionsPort.getPasskeyCreationOptions(user, challenge);
        return new RegistrationResponse(passkeyOptions, challenge.sessionId());
    }
}