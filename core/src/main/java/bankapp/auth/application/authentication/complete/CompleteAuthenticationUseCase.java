package bankapp.auth.application.authentication.complete;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;

import java.util.UUID;

public class CompleteAuthenticationUseCase {

    private final SessionRepository sessionRepository;
    private final PasskeyRepository passkeyRepository;
    private final WebAuthnVerificationPort webAuthnVerificationPort;
    private final TokenIssuingPort tokenIssuingPort;

    public CompleteAuthenticationUseCase(SessionRepository sessionRepo, WebAuthnVerificationPort webAuthnVerificationPort, PasskeyRepository passkeyRepository, TokenIssuingPort tokenIssuingPort) {
        this.sessionRepository = sessionRepo;
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

        sessionRepository.delete(command.sessionId());

        AuthTokens tokens = tokenIssuingPort.issueTokensForUser(userId);
        return new AuthenticationGrant(tokens);
    }

    private Passkey verifyChallengeAndUpdateCredentialRecord(CompleteAuthenticationCommand command, Session session) {
        try {
            var credentialRecordOpt = passkeyRepository.load(command.credentialId());

            var credentialRecord = credentialRecordOpt.orElseThrow();

            return webAuthnVerificationPort.confirmAuthenticationChallenge(command.AuthenticationResponseJSON(), session, credentialRecord);
        } catch (RuntimeException e) {
            throw new CompleteAuthenticationException("Failed to confirm authentication challenge: " + e.getMessage(), e);
        }
    }

    private Session getSession(CompleteAuthenticationCommand command) {
        var sessionOptional = sessionRepository.load(command.sessionId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteAuthenticationException("No such session with ID: " + command.sessionId());
        }
        return sessionOptional.get();
    }
}
