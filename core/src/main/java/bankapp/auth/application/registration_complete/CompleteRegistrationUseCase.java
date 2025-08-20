package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;

public class CompleteRegistrationUseCase {

    private final SessionRepository sessionRepository;
    private final WebAuthnPort webAuthnPort;

    public CompleteRegistrationUseCase(
            SessionRepository sessionRepository,
            WebAuthnPort webAuthnPort
    ) {
        this.sessionRepository = sessionRepository;
        this.webAuthnPort = webAuthnPort;
    }

    public void handle(CompleteRegistrationCommand command) {
        var session = sessionRepository.load(command.sessionId());
        if (session.isEmpty()) {
            throw new CompleteRegistrationException("No such session");
        }
        try {
            webAuthnPort.verify(command.publicKeyCredentialJson(), session.get());
        } catch (Exception e) {
            throw new CompleteRegistrationException("Failed to verify new credential registration: " + e.getMessage(), e);
        }
    }

}
//check if session is present
// ??
