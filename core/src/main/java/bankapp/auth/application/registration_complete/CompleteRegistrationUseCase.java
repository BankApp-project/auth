package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verification_complete.port.out.CredentialRepository;

public class CompleteRegistrationUseCase {

    private final SessionRepository sessionRepository;
    private final WebAuthnPort webAuthnPort;
    private final CredentialRepository credentialRepository;

    public CompleteRegistrationUseCase(
            SessionRepository sessionRepository,
            WebAuthnPort webAuthnPort,
            CredentialRepository credentialRepository) {
        this.sessionRepository = sessionRepository;
        this.webAuthnPort = webAuthnPort;
        this.credentialRepository = credentialRepository;
    }

    public void handle(CompleteRegistrationCommand command) {
        var session = getSession(command);

        CredentialRecord credential = verifyAndExtractCredentialRecord(command, session);

        saveCredentialRecord(credential);
        sessionRepository.delete(command.sessionId());
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
