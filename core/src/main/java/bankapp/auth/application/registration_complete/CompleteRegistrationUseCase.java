package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.LoggerPort;
import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.verification_complete.port.out.CredentialRepository;
import bankapp.auth.application.verification_complete.port.out.UserRepository;
import bankapp.auth.domain.model.User;

public class CompleteRegistrationUseCase {

    private final SessionRepository sessionRepository;
    private final WebAuthnPort webAuthnPort;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final TokenIssuingPort tokenIssuer;
    private final LoggerPort log;

    public CompleteRegistrationUseCase(
            SessionRepository sessionRepository,
            WebAuthnPort webAuthnPort,
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            TokenIssuingPort tokenIssuingPort,
            LoggerPort log
    ) {
        this.sessionRepository = sessionRepository;
        this.webAuthnPort = webAuthnPort;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuingPort;
        this.log = log;
    }

    public RegistrationResult handle(CompleteRegistrationCommand command) {
        log.info("Finalizing registration for session ID: {}", command.sessionId());

        var session = getSession(command);
        log.debug("Loaded registration session for user: {}", session.userId());

        CredentialRecord credential = verifyAndExtractCredentialRecord(command, session);
        log.debug("New credential verified for user: {}", session.userId());

        saveCredentialRecord(credential);
        log.debug("Credential record persisted for user: {}", session.userId());

        sessionRepository.delete(command.sessionId());
        log.debug("Consumed registration session removed: {}", command.sessionId());

        User activatedUser = fetchAndActivateUser(credential.userHandle());
        log.debug("User account activated: {}", activatedUser.getId());

        var tokens = generateTokensForUser(activatedUser);
        log.debug("Authentication tokens created for user: {}", activatedUser.getId());

        log.info("Registration finalized and tokens issued for user: {}", activatedUser.getId());
        return new RegistrationResult(tokens);
    }



    private AuthTokens generateTokensForUser(User activatedUser) {
        return tokenIssuer.issueTokensForUser(activatedUser.getId());
    }

    private User fetchAndActivateUser(byte[] userHandle) {
        var userId = ByteArrayUtil.bytesToUuid(userHandle);

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new CompleteRegistrationException("User does not exists");
        }
        var user = userOpt.get();

        user.setEnabled(true);

        userRepository.save(user);

        return user;
    }

    private AuthSession getSession(CompleteRegistrationCommand command) {
        var session = sessionRepository.load(command.sessionId());
        if (session.isEmpty()) {
            throw new CompleteRegistrationException("No such session");
        }
        return session.get();
    }

    private CredentialRecord verifyAndExtractCredentialRecord(CompleteRegistrationCommand command, AuthSession session) {
        CredentialRecord credential;
        try {
            credential =  webAuthnPort.confirmRegistrationChallenge(command.publicKeyCredentialJson(), session);
        } catch (Exception e) {
            throw new CompleteRegistrationException("Failed to confirm new credential registration: " + e.getMessage(), e);
        }

        return credential;
    }

    private void saveCredentialRecord(CredentialRecord credential) {
        try {
            credentialRepository.save(credential);
        } catch (RuntimeException e) {
            throw new CompleteRegistrationException("Failed to save credential: " + e.getMessage(), e);
        }
    }
}
