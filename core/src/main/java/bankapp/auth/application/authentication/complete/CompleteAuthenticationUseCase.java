package bankapp.auth.application.authentication.complete;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;

import java.util.UUID;

public class CompleteAuthenticationUseCase {

    private final ChallengeRepository challengeRepository;
    private final PasskeyRepository passkeyRepository;
    private final WebAuthnVerificationPort webAuthnVerificationPort;
    private final TokenIssuingPort tokenIssuingPort;

    public CompleteAuthenticationUseCase(ChallengeRepository sessionRepo, WebAuthnVerificationPort webAuthnVerificationPort, PasskeyRepository passkeyRepository, TokenIssuingPort tokenIssuingPort) {
        this.challengeRepository = sessionRepo;
        this.webAuthnVerificationPort = webAuthnVerificationPort;
        this.passkeyRepository = passkeyRepository;
        this.tokenIssuingPort = tokenIssuingPort;
    }

    @TransactionalUseCase
    public AuthenticationGrant handle(CompleteAuthenticationCommand command) {
        var session = getSession(command);

        var updatedCredential = verifyChallengeAndUpdateCredentialRecord(command, session);

        UUID userId = updatedCredential.getUserHandle();

        passkeyRepository.updateSignCount(updatedCredential);

        challengeRepository.delete(command.challengeId());

        AuthTokens tokens = tokenIssuingPort.issueTokensForUser(userId);
        return new AuthenticationGrant(tokens);
    }

    private Passkey verifyChallengeAndUpdateCredentialRecord(CompleteAuthenticationCommand command, Challenge session) {
        try {
            var credentialRecordOpt = passkeyRepository.load(command.credentialId());

            var credentialRecord = credentialRecordOpt.orElseThrow();

            return webAuthnVerificationPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session, credentialRecord);
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
