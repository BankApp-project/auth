package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;

import java.util.UUID;

public class CompleteAuthenticationUseCase {

    private final ChallengeRepository challengeRepository;
    private final CredentialRepository credentialRepository;
    private final WebAuthnPort webAuthnPort;
    private final TokenIssuingPort tokenIssuingPort;

    public CompleteAuthenticationUseCase(ChallengeRepository sessionRepo, WebAuthnPort webAuthnPort, CredentialRepository credentialRepository, TokenIssuingPort tokenIssuingPort) {
        this.challengeRepository = sessionRepo;
        this.webAuthnPort = webAuthnPort;
        this.credentialRepository = credentialRepository;
        this.tokenIssuingPort = tokenIssuingPort;
    }

    public CompleteAuthenticationResponse handle(CompleteAuthenticationCommand command) {
        var session = getSession(command);

        var updatedCredential = verifyChallengeAndUpdateCredentialRecord(command, session);

        UUID userId = updatedCredential.getUserHandle();

        credentialRepository.save(updatedCredential);

        challengeRepository.delete(command.sessionId());

        AuthTokens tokens = tokenIssuingPort.issueTokensForUser(userId);
        return new CompleteAuthenticationResponse(tokens);
    }

    private Passkey verifyChallengeAndUpdateCredentialRecord(CompleteAuthenticationCommand command, Challenge session) {
        try {
            var credentialRecord = credentialRepository.load(command.credentialId());
            return webAuthnPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session, credentialRecord);
        } catch (RuntimeException e) {
            throw new CompleteAuthenticationException("Failed to confirm authentication challenge: " + e.getMessage(), e);
        }
    }

    private Challenge getSession(CompleteAuthenticationCommand command) {
        var sessionOptional = challengeRepository.load(command.sessionId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteAuthenticationException("No such session with ID: " + command.sessionId());
        }
        return sessionOptional.get();
    }
}
