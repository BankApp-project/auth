package bankapp.auth.application.verification.complete;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.LoginResponse;
import bankapp.auth.application.shared.port.out.dto.RegistrationResponse;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.repository.PasskeyRepository;
import bankapp.auth.application.shared.port.out.repository.SessionRepository;
import bankapp.auth.application.shared.port.out.repository.UserRepository;
import bankapp.auth.application.shared.port.out.service.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.service.PasskeyOptionsPort;
import bankapp.auth.application.shared.port.out.service.SessionIdGenerationPort;
import bankapp.auth.application.verification.complete.port.in.CompleteVerificationCommand;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.UUID;

@UseCase
public class CompleteVerificationUseCase {

    private final OtpService otpService;

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    private final PasskeyOptionsPort passkeyOptionsPort;
    private final ChallengeGenerationPort challengeGenerator;
    private final SessionIdGenerationPort sessionIdGenerator;

    public CompleteVerificationUseCase(
            @NotNull SessionRepository sessionRepository,
            @NotNull PasskeyRepository passkeyRepository,
            @NotNull UserRepository userRepository,
            @NotNull PasskeyOptionsPort passkeyOptionsPort,
            @NotNull ChallengeGenerationPort challengeGenerator,
            @NotNull OtpService otpService,
            @NotNull SessionIdGenerationPort sessionIdGenerator
    ) {
        this.sessionRepository = sessionRepository;
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
        this.passkeyOptionsPort = passkeyOptionsPort;
        this.challengeGenerator = challengeGenerator;
        this.otpService = otpService;
        this.sessionIdGenerator = sessionIdGenerator;
    }

    @TransactionalUseCase
    public CompleteVerificationResponse handle(CompleteVerificationCommand command) {

        verifyAndConsumeOtp(command.key(), command.value());

        User user = findOrCreateUser(command.key());

        var challenge = generateChallenge();
        var session = prepareSession(challenge, user.getId());
        saveSession(session);

        return prepareResponse(user, session);
    }

    private Challenge generateChallenge() {
        return challengeGenerator.generate();
    }

    private Session prepareSession(Challenge challenge, UUID userId) {
        var sessionId = sessionIdGenerator.generate();

        return new Session(
                sessionId,
                challenge,
                userId
        );
    }

    private void verifyAndConsumeOtp(EmailAddress key, String value) {
        otpService.verifyAndConsumeOtp(key,value);
    }

    private User findOrCreateUser(EmailAddress email) {
        var userOptional = userRepository.findByEmail(email);
        return userOptional.orElseGet(() -> createAndSaveUser(email));
    }

    private User createAndSaveUser(EmailAddress email) {
        User user = User.createNew(email);
        userRepository.save(user);
        return user;
    }

    private void saveSession(Session session) {
        sessionRepository.save(session);
    }

    private CompleteVerificationResponse prepareResponse(User user, Session session) {
        if (user.isEnabled()) {
            return getLoginResponse(user, session);
        } else {
            return getRegistrationResponse(user, session);
        }
    }

    private LoginResponse getLoginResponse(User user, Session session) {
        var userCredentials = passkeyRepository.loadForUserId(user.getId());
        var passkeyOptions = passkeyOptionsPort.getPasskeyRequestOptions(userCredentials, session);
        return new LoginResponse(passkeyOptions, session.sessionId());
    }

    private RegistrationResponse getRegistrationResponse(User user, Session session) {
        var passkeyOptions = passkeyOptionsPort.getPasskeyCreationOptions(user, session);
        return new RegistrationResponse(passkeyOptions, session.sessionId());
    }
}