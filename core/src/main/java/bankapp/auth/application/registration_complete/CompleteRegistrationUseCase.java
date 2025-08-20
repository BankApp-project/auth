package bankapp.auth.application.registration_complete;

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
        var session = sessionRepository.load(command.sessionId());
        if (session.isEmpty()) {
            throw new CompleteRegistrationException("No such session");
        }

        CredentialRecord credential;
        try {
            credential =  webAuthnPort.verify(command.publicKeyCredentialJson(), session.get());
        } catch (Exception e) {
            throw new CompleteRegistrationException("Failed to verify new credential registration: " + e.getMessage(), e);
        }

        credentialRepository.save(credential);
    }
}
