package bankapp.auth.application.authentication.initiate;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verification.complete.port.SessionIdGenerationPort;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification.complete.port.out.PasskeyOptionsPort;
import bankapp.auth.application.verification.complete.port.out.dto.LoginResponse;
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
        var challenge = challengeGenerator.generate();
        var session = generateSession(challenge);

        sessionRepository.save(session);

        var passkeyRequestOptions = credentialOptionsService.getPasskeyRequestOptions(session);
        return new LoginResponse(passkeyRequestOptions, session.sessionId());
    }

    private Session generateSession(Challenge challenge) {
        var sessionId = sessionIdGenerator.generate();

        return new Session(
                sessionId,
                challenge
        );
    }
}
