package bankapp.auth.application.verification.complete;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.verification.complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification.complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification.complete.port.out.dto.LoginResponse;
import bankapp.auth.application.verification.complete.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.UUID;

@UseCase
public class CompleteVerificationUseCase {

    private final OtpService otpService;

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    private final CredentialOptionsPort credentialOptionsPort;
    private final ChallengeGenerationPort challengeGenerator;

    public CompleteVerificationUseCase(
            @NotNull ChallengeRepository challengeRepository,
            @NotNull PasskeyRepository passkeyRepository,
            @NotNull UserRepository userRepository,
            @NotNull CredentialOptionsPort credentialOptionsPort,
            @NotNull ChallengeGenerationPort challengeGenerator,
            @NotNull OtpService otpService
    ) {
        this.challengeRepository = challengeRepository;
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
        this.credentialOptionsPort = credentialOptionsPort;
        this.challengeGenerator = challengeGenerator;
        this.otpService = otpService;
    }

    //TODO THINK ABOUT DIVIDING IT TO VERIFICATION_COMPLETE AND REGISTRATION_INITIATE / AUTHENTICATION_INITIATE
    @TransactionalUseCase
    public CompleteVerificationResponse handle(CompleteVerificationCommand command) {

        verifyAndConsumeOtp(command.key(), command.value());

        User user = findOrCreateUser(command.key());

        var challenge = generateChallenge();
        var session = prepareSession(challenge, user.getId());
        challengeRepository.save(session);

        return prepareResponse(user, session);
    }

    private Challenge generateChallenge() {
        return challengeGenerator.generate();
    }

    private Session prepareSession(Challenge challenge, UUID userId) {
//        var sessionId = sessionIdGenerator.generate();
        var sessionId = UUID.randomUUID();


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

    private CompleteVerificationResponse prepareResponse(User user, Session session) {
        if (user.isEnabled()) {
            return getLoginResponse(user, session);
        } else {
            return getRegistrationResponse(user, session);
        }
    }

    private LoginResponse getLoginResponse(User user, Session session) {
        var userCredentials = passkeyRepository.loadForUserId(user.getId());
        var passkeyOptions = credentialOptionsPort.getPasskeyRequestOptions(userCredentials, session);
        return new LoginResponse(passkeyOptions, session.sessionId());
    }

    private RegistrationResponse getRegistrationResponse(User user, Session session) {
        var passkeyOptions = credentialOptionsPort.getPasskeyCreationOptions(user, session);
        return new RegistrationResponse(passkeyOptions, session.sessionId());
    }
}