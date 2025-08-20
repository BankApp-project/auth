package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;

public class CompleteAuthenticationUseCase {

    private SessionRepository sessionRepository;
    private WebAuthnPort webAuthnPort;

    public CompleteAuthenticationUseCase(SessionRepository sessionRepo, WebAuthnPort webAuthnPort) {
        this.sessionRepository = sessionRepo;
        this.webAuthnPort = webAuthnPort;
    }

    public CompleteAuthenticationResponse handle(CompleteAuthenticationCommand command) {
        var sessionOptional = sessionRepository.load(command.sessionId());
        var session = sessionOptional.get();
        webAuthnPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session);

        return null;
    }
}
