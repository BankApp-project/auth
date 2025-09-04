package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;

import java.util.UUID;

public class CompleteAuthenticationUseCase {

    private final ChallengeRepository challengeRepository;
    private final PasskeyRepository passkeyRepository;
    private final WebAuthnPort webAuthnPort;
    private final TokenIssuingPort tokenIssuingPort;

    public CompleteAuthenticationUseCase(ChallengeRepository sessionRepo, WebAuthnPort webAuthnPort, PasskeyRepository passkeyRepository, TokenIssuingPort tokenIssuingPort) {
        this.challengeRepository = sessionRepo;
        this.webAuthnPort = webAuthnPort;
        this.passkeyRepository = passkeyRepository;
        this.tokenIssuingPort = tokenIssuingPort;
    }

    public AuthenticationGrant handle(CompleteAuthenticationCommand command) {
        var session = getSession(command);

        var updatedCredential = verifyChallengeAndUpdateCredentialRecord(command, session);

        UUID userId = updatedCredential.getUserHandle();

        passkeyRepository.update(updatedCredential);

        challengeRepository.delete(command.challengeId());

        AuthTokens tokens = tokenIssuingPort.issueTokensForUser(userId);
        return new AuthenticationGrant(tokens);
    }

    private Passkey verifyChallengeAndUpdateCredentialRecord(CompleteAuthenticationCommand command, Challenge session) {
        try {
            var credentialRecordOpt = passkeyRepository.load(command.credentialId());

            var credentialRecord = credentialRecordOpt.orElseThrow();

            return webAuthnPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session, credentialRecord);
        } catch (RuntimeException e) {
            throw new CompleteAuthenticationException("Failed to confirm authentication value: " + e.getMessage(), e);
        }
    }

    private Challenge getSession(CompleteAuthenticationCommand command) {
        var sessionOptional = challengeRepository.load(command.challengeId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteAuthenticationException("No such session with ID: " + command.challengeId());
        }
        return sessionOptional.get();
    }
}
