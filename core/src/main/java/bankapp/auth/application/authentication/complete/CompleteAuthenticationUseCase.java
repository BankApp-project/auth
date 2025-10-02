package bankapp.auth.application.authentication.complete;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.AuthenticationGrant;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.repository.PasskeyRepository;
import bankapp.auth.application.shared.port.out.repository.SessionRepository;
import bankapp.auth.application.shared.port.out.service.PasskeyVerificationPort;
import bankapp.auth.application.shared.port.out.service.TokenIssuingPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;

import java.util.UUID;

@UseCase
public class CompleteAuthenticationUseCase {

    private final SessionRepository sessionRepository;
    private final PasskeyRepository passkeyRepository;
    private final PasskeyVerificationPort passkeyVerificationPort;
    private final TokenIssuingPort tokenIssuingPort;

    public CompleteAuthenticationUseCase(SessionRepository sessionRepo, PasskeyVerificationPort passkeyVerificationPort, PasskeyRepository passkeyRepository, TokenIssuingPort tokenIssuingPort) {
        this.sessionRepository = sessionRepo;
        this.passkeyVerificationPort = passkeyVerificationPort;
        this.passkeyRepository = passkeyRepository;
        this.tokenIssuingPort = tokenIssuingPort;
    }

    @TransactionalUseCase
    public AuthenticationGrant handle(CompleteAuthenticationCommand command) {
        var verifiedPasskey = verifyAndUpdatePasskey(command);

        deleteSession(command.sessionId());

        AuthTokens tokens = generateTokensForUser(verifiedPasskey.getUserHandle());
        return new AuthenticationGrant(tokens);
    }

    private Passkey verifyAndUpdatePasskey(CompleteAuthenticationCommand command) {
        try {
            return verifyPasskeyAndUpdateSignCount(command);
        } catch (MaliciousCounterException e) {
            //todo add logic for dealing with malicious counters
            throw e;
        } catch (RuntimeException e) {
            throw new CompleteAuthenticationException("Failed to confirm authentication challenge: " + e.getMessage(), e);
        }
    }

    private Passkey verifyPasskeyAndUpdateSignCount(CompleteAuthenticationCommand command) {
        var session = getSession(command);
        var passkey = getPasskey(command);

        var verifiedPasskey = passkeyVerificationPort.handleAuthentication(command.AuthenticationResponseJSON(), session, passkey);
        passkeyRepository.updateSignCount(verifiedPasskey);

        return verifiedPasskey;
    }

    private Session getSession(CompleteAuthenticationCommand command) {
        var sessionOptional = sessionRepository.load(command.sessionId());
        if (sessionOptional.isEmpty()) {
            throw new CompleteAuthenticationException("No such session with ID: " + command.sessionId());
        }
        return sessionOptional.get();
    }

    private Passkey getPasskey(CompleteAuthenticationCommand command) {
        var passkeyOptional = passkeyRepository.load(command.credentialId());

        return passkeyOptional.orElseThrow();
    }

    private void deleteSession(UUID sessionId) {
        sessionRepository.delete(sessionId);
    }

    private AuthTokens generateTokensForUser(UUID userId) {
        return tokenIssuingPort.issueTokensForUser(userId);
    }
}
