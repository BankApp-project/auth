package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.registration_complete.port.in.CompleteRegistrationCommand;
import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import lombok.NonNull;

import java.util.UUID;

public class CompleteRegistrationUseCase {

    private final ChallengeRepository challengeRepository;
    private final WebAuthnPort webAuthnPort;
    private final PasskeyRepository passkeyRepository;
    private final UserRepository userRepository;
    private final TokenIssuingPort tokenIssuer;

    public CompleteRegistrationUseCase(
            ChallengeRepository challengeRepository,
            WebAuthnPort webAuthnPort,
            PasskeyRepository passkeyRepository,
            UserRepository userRepository,
            TokenIssuingPort tokenIssuingPort
    ) {
        this.challengeRepository = challengeRepository;
        this.webAuthnPort = webAuthnPort;
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuingPort;
    }

    public AuthenticationGrant handle(@NonNull CompleteRegistrationCommand command) {
        var challenge = getChallenge(command);

        var credential = verifyAndExtractCredentialRecord(command, challenge);

        saveCredentialRecord(credential);

        challengeRepository.delete(command.challengeId());

        User user = fetchUser(credential.userHandle());

        user.activate();

        userRepository.save(user);

        var tokens = generateTokensForUser(user);

        return new AuthenticationGrant(tokens);
    }

    private AuthTokens generateTokensForUser(User activatedUser) {
        return tokenIssuer.issueTokensForUser(activatedUser.getId());
    }

    private User fetchUser(UUID userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new CompleteRegistrationException("User does not exists for userId: " + userId);
        }
        return userOpt.get();
    }

    private Challenge getChallenge(CompleteRegistrationCommand command) {
        var challenge = challengeRepository.load(command.challengeId());
        if (challenge.isEmpty()) {
            throw new CompleteRegistrationException("No such value with ID: " + command.challengeId());
        }
        return challenge.get();
    }

    private PasskeyRegistrationData verifyAndExtractCredentialRecord(CompleteRegistrationCommand command, Challenge challenge) {
        PasskeyRegistrationData credential;
        try {
            credential = webAuthnPort.confirmRegistrationChallenge(command.RegistrationResponseJSON(), challenge);
        } catch (Exception e) {
            throw new CompleteRegistrationException("Failed to confirm new credential registration: " + e.getMessage(), e);
        }

        return credential;
    }

    private void saveCredentialRecord(PasskeyRegistrationData credential) {
        try {
            passkeyRepository.save(credential);
        } catch (CredentialAlreadyExistsException e) {
            throw new CompleteRegistrationException("Credential already exists with this ID or PublicKey. Aborted.");
        } catch (RuntimeException e) {
            throw new CompleteRegistrationException("Failed to save credential: " + e.getMessage(), e);
        }
    }
}
