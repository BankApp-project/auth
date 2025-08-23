package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;

import java.time.Clock;

public class InitiateAuthenticationUseCase {

    private final Clock clock;
    private final long challengeTtl;

    private final ChallengeGenerationPort challengeGenerator;
    private final ChallengeRepository challengeRepository;
    private final CredentialOptionsPort credentialOptionsService;

    public InitiateAuthenticationUseCase(Clock clock, long challengeTtl, ChallengeGenerationPort challengeGenerator, ChallengeRepository challengeRepository, CredentialOptionsPort credentialOptionsService) {
        this.clock = clock;
        this.challengeTtl = challengeTtl;
        this.challengeGenerator = challengeGenerator;
        this.challengeRepository = challengeRepository;
        this.credentialOptionsService = credentialOptionsService;
    }

    LoginResponse handle(InitiateAuthenticationCommand command) {
        var challenge = challengeGenerator.generate(clock, challengeTtl);

        challengeRepository.save(challenge);

        var passkeyRequestOptions = credentialOptionsService.getPasskeyRequestOptions(challenge);
        return new LoginResponse(passkeyRequestOptions, challenge.sessionId());
    }
}
