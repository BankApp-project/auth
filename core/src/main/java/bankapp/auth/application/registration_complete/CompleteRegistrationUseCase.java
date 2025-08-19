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
        webAuthnPort.verify(command.publicKeyCredentialJson(), session.get());
    }
}