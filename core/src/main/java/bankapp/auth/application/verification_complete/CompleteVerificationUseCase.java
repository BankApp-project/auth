package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.application.verification_complete.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;
import bankapp.auth.domain.model.vo.EmailAddress;

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

        var challenge = generateAndSaveChallenge();

        return prepareResponse(user, challenge);
    }

    private void verifyAndConsumeOtp(EmailAddress key, String value) {
        otpService.verifyAndConsumeOtp(key,value);
    }

    private Challenge generateAndSaveChallenge() {
        var challenge = challengeGenerator.generate();
        challengeRepository.save(challenge);
        return challenge;
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

    private CompleteVerificationResponse prepareResponse(User user, Challenge challenge) {
        if (user.isEnabled()) {
            return getLoginResponse(user, challenge);
        } else {
            return getRegistrationResponse(user, challenge);
        }
    }

    private LoginResponse getLoginResponse(User user, Challenge challenge) {
        var userCredentials = passkeyRepository.loadForUserId(user.getId());
        var passkeyOptions = credentialOptionsPort.getPasskeyRequestOptions(userCredentials, challenge);
        return new LoginResponse(passkeyOptions, challenge.sessionId());
    }

    private RegistrationResponse getRegistrationResponse(User user, Challenge challenge) {
        var passkeyOptions = credentialOptionsPort.getPasskeyCreationOptions(user, challenge);
        return new RegistrationResponse(passkeyOptions, challenge.sessionId());
    }
}