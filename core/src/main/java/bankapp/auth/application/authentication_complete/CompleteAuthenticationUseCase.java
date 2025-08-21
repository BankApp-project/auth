package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;

public class CompleteAuthenticationUseCase {

    private final SessionRepository sessionRepository;
    private final CredentialRepository credentialRepository;
    private final WebAuthnPort webAuthnPort;

    public CompleteAuthenticationUseCase(SessionRepository sessionRepo, WebAuthnPort webAuthnPort, CredentialRepository credentialRepository) {
        this.sessionRepository = sessionRepo;
        this.webAuthnPort = webAuthnPort;
        this.credentialRepository = credentialRepository;
    }

    public CompleteAuthenticationResponse handle(CompleteAuthenticationCommand command) {
        var sessionOptional = sessionRepository.load(command.sessionId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteAuthenticationException("No such session with ID: " + command.sessionId());
        }
        var session = sessionOptional.get();

        var credentialRecord = credentialRepository.load(command.credentialId());
        try {
            webAuthnPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session, credentialRecord);
        } catch (RuntimeException e) {
            throw new CompleteAuthenticationException("Failed to confirm authentication challenge: " + e.getMessage(), e);
        }
        return null;
    }
}
