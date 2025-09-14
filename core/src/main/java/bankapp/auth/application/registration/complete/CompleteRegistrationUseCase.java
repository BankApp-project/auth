package bankapp.auth.application.registration.complete;

import bankapp.auth.application.registration.complete.port.in.CompleteRegistrationCommand;
import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;
import lombok.NonNull;

import java.util.UUID;

public class CompleteRegistrationUseCase {

    private final SessionRepository sessionRepository;
    private final WebAuthnVerificationPort webAuthnVerificationPort;
    private final PasskeyRepository passkeyRepository;
    private final UserRepository userRepository;
    private final TokenIssuingPort tokenIssuer;

    public CompleteRegistrationUseCase(
            SessionRepository sessionRepository,
            WebAuthnVerificationPort webAuthnVerificationPort,
            PasskeyRepository passkeyRepository,
            UserRepository userRepository,
            TokenIssuingPort tokenIssuingPort
    ) {
        this.sessionRepository = sessionRepository;
        this.webAuthnVerificationPort = webAuthnVerificationPort;
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuingPort;
    }

    @TransactionalUseCase
    public AuthenticationGrant handle(@NonNull CompleteRegistrationCommand command) {
        var challenge = getChallenge(command);

        var credential = verifyAndExtractCredentialRecord(command, challenge);

        saveCredentialRecord(credential);

        sessionRepository.delete(command.sessionId());

        User user = fetchUser(credential.getUserHandle());

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

    private Session getChallenge(CompleteRegistrationCommand command) {
        var challenge = sessionRepository.load(command.sessionId());
        if (challenge.isEmpty()) {
            throw new CompleteRegistrationException("No such challenge with ID: " + command.sessionId());
        }
        return challenge.get();
    }

    private Passkey verifyAndExtractCredentialRecord(CompleteRegistrationCommand command, Session session) {
        Passkey credential;
        try {
            credential = webAuthnVerificationPort.confirmRegistrationChallenge(command.RegistrationResponseJSON(), session);
        } catch (Exception e) {
            throw new CompleteRegistrationException("Failed to confirm new credential registration: " + e.getMessage(), e);
        }

        return credential;
    }

    private void saveCredentialRecord(Passkey credential) {
        try {
            passkeyRepository.save(credential);
        } catch (CredentialAlreadyExistsException e) {
            throw new CompleteRegistrationException("Credential already exists with this ID or PublicKey. Aborted.");
        } catch (RuntimeException e) {
            throw new CompleteRegistrationException("Failed to save credential: " + e.getMessage(), e);
        }
    }
}
