package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;

public class CompleteAuthenticationUseCase {

    private final SessionRepository sessionRepository;
    private final CredentialRepository credentialRepository;
    private final WebAuthnPort webAuthnPort;
    private final TokenIssuingPort tokenIssuingPort;

    public CompleteAuthenticationUseCase(SessionRepository sessionRepo, WebAuthnPort webAuthnPort, CredentialRepository credentialRepository, TokenIssuingPort tokenIssuingPort) {
        this.sessionRepository = sessionRepo;
        this.webAuthnPort = webAuthnPort;
        this.credentialRepository = credentialRepository;
        this.tokenIssuingPort = tokenIssuingPort;
    }

    public CompleteAuthenticationResponse handle(CompleteAuthenticationCommand command) {
        var session = getSession(command);

        var updatedCredential = verifyChallengeAndUpdateCredentialRecord(command, session);

        credentialRepository.save(updatedCredential);

        sessionRepository.delete(command.sessionId());

        AuthTokens tokens = tokenIssuingPort.issueTokensForUser(session.userId());

        return new CompleteAuthenticationResponse(tokens);
    }

    private CredentialRecord verifyChallengeAndUpdateCredentialRecord(CompleteAuthenticationCommand command, AuthSession session) {
        try {
            var credentialRecord = credentialRepository.load(command.credentialId());
            return webAuthnPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session, credentialRecord);
        } catch (RuntimeException e) {
            throw new CompleteAuthenticationException("Failed to confirm authentication challenge: " + e.getMessage(), e);
        }
    }

    private AuthSession getSession(CompleteAuthenticationCommand command) {
        var sessionOptional = sessionRepository.load(command.sessionId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteAuthenticationException("No such session with ID: " + command.sessionId());
        }
        return sessionOptional.get();
    }
}
