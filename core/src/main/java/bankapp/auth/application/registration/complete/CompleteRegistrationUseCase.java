package bankapp.auth.application.registration.complete;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.shared.port.out.service.PasskeyVerificationPort;
import bankapp.auth.application.shared.port.out.service.TokenIssuingPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;
import lombok.NonNull;

import java.util.UUID;

@UseCase
public class CompleteRegistrationUseCase {

    private final SessionRepository sessionRepository;
    private final PasskeyVerificationPort passkeyVerificationPort;
    private final PasskeyRepository passkeyRepository;
    private final UserRepository userRepository;
    private final TokenIssuingPort tokenIssuer;

    public CompleteRegistrationUseCase(
            SessionRepository sessionRepository,
            PasskeyVerificationPort passkeyVerificationPort,
            PasskeyRepository passkeyRepository,
            UserRepository userRepository,
            TokenIssuingPort tokenIssuingPort
    ) {
        this.sessionRepository = sessionRepository;
        this.passkeyVerificationPort = passkeyVerificationPort;
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuingPort;
    }

    @TransactionalUseCase
    public AuthenticationGrant handle(@NonNull CompleteRegistrationCommand command) {
        var session = getSession(command);

        var credential = verifyAndExtractPasskey(command, session);

        savePasskey(credential);

        deleteSession(command.sessionId());

        User user = fetchUser(credential.getUserHandle());

        activateUser(user);

        var tokens = generateTokensForUser(user);

        return new AuthenticationGrant(tokens);
    }

    private Session getSession(CompleteRegistrationCommand command) {
        var sessionOptional = sessionRepository.load(command.sessionId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteRegistrationException("No such session with ID: " + command.sessionId());
        }
        return sessionOptional.get();
    }

    private Passkey verifyAndExtractPasskey(CompleteRegistrationCommand command, Session session) {
        Passkey credential;
        try {
            credential = passkeyVerificationPort.handleRegistration(command.RegistrationResponseJSON(), session);
        } catch (Exception e) {
            throw new CompleteRegistrationException("Failed to confirm new credential registration: " + e.getMessage(), e);
        }

        return credential;
    }

    private void savePasskey(Passkey credential) {
        try {
            passkeyRepository.save(credential);
        } catch (CredentialAlreadyExistsException e) {
            throw new CompleteRegistrationException("Credential already exists with this ID or PublicKey. Aborted.");
        } catch (RuntimeException e) {
            throw new CompleteRegistrationException("Failed to save credential: " + e.getMessage(), e);
        }
    }

    private void deleteSession(UUID sessionId) {
        sessionRepository.delete(sessionId);
    }

    private User fetchUser(UUID userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new CompleteRegistrationException("User does not exists for userId: " + userId);
        }
        return userOpt.get();
    }

    private void activateUser(User user) {
        user.activate();

        userRepository.save(user);
    }

    private AuthTokens generateTokensForUser(User activatedUser) {
        return tokenIssuer.issueTokensForUser(activatedUser.getId());
    }

}
