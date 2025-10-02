package bankapp.auth.application.authentication.initiate;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.dto.LoginResponse;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.repository.SessionRepository;
import bankapp.auth.application.shared.port.out.service.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.service.PasskeyOptionsPort;
import bankapp.auth.application.shared.port.out.service.SessionIdGenerationPort;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;

@UseCase
public class InitiateAuthenticationUseCase {

    private final ChallengeGenerationPort challengeGenerator;
    private final SessionRepository sessionRepository;
    private final PasskeyOptionsPort credentialOptionsService;
    private final SessionIdGenerationPort sessionIdGenerator;

    public InitiateAuthenticationUseCase(
            ChallengeGenerationPort challengeGenerator,
            SessionRepository sessionRepository,
            PasskeyOptionsPort credentialOptionsService,
            SessionIdGenerationPort sessionIdGenerator
    ) {
        this.challengeGenerator = challengeGenerator;
        this.sessionRepository = sessionRepository;
        this.credentialOptionsService = credentialOptionsService;
        this.sessionIdGenerator = sessionIdGenerator;
    }

    @TransactionalUseCase
    public LoginResponse handle() {
        var session = generateSessionWithChallenge();

        var passkeyRequestOptions = credentialOptionsService.getPasskeyRequestOptions(session);

        return new LoginResponse(passkeyRequestOptions, session.sessionId());
    }

    private Session generateSessionWithChallenge() {
        var challenge = challengeGenerator.generate();
        var sessionId = sessionIdGenerator.generate();
        var session = new Session(
                sessionId,
                challenge
        );
        sessionRepository.save(session);
        return session;
    }
}
