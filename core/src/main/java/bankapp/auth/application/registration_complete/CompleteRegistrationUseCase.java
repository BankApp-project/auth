package bankapp.auth.application.registration_complete;

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

    public CompleteRegistrationUseCase(
            SessionRepository sessionRepository,
            WebAuthnPort webAuthnPort,
            CredentialRepository credentialRepository,
            UserRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.webAuthnPort = webAuthnPort;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    public void handle(CompleteRegistrationCommand command) {
        var session = getSession(command);

        CredentialRecord credential = verifyAndExtractCredentialRecord(command, session);

        saveCredentialRecord(credential);
        sessionRepository.delete(command.sessionId());

        var userId = ByteArrayUtil.bytesToUuid(credential.userHandle());
        var userOpt = userRepository.findById(userId);
        var user = userOpt.get();
        user.setEnabled(true);
        userRepository.save(user);
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
