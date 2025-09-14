package bankapp.auth.application.authentication.initiate;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification.complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification.complete.port.out.dto.LoginResponse;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;

import java.util.UUID;

@UseCase
public class InitiateAuthenticationUseCase {

    private final ChallengeGenerationPort challengeGenerator;
    private final ChallengeRepository challengeRepository;
    private final CredentialOptionsPort credentialOptionsService;

    public InitiateAuthenticationUseCase(
            ChallengeGenerationPort challengeGenerator,
            ChallengeRepository challengeRepository,
            CredentialOptionsPort credentialOptionsService) {
        this.challengeGenerator = challengeGenerator;
        this.challengeRepository = challengeRepository;
        this.credentialOptionsService = credentialOptionsService;
    }

    @TransactionalUseCase
    public LoginResponse handle() {
        var challenge = challengeGenerator.generate();
        var session = generateSession(challenge);

        challengeRepository.save(session);

        var passkeyRequestOptions = credentialOptionsService.getPasskeyRequestOptions(session);
        return new LoginResponse(passkeyRequestOptions, session.sessionId());
    }

    private Session generateSession(Challenge challenge) {
        var sessionId = UUID.randomUUID();

        return new Session(
                sessionId,
                challenge
        );
    }
}
