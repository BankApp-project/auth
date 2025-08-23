package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

import java.time.Clock;

public class InitiateAuthenticationUseCase {

    private final Clock clock;
    private final long challengeTtl;

    private final ChallengeGenerationPort challengeGenerator;
    private final ChallengeRepository challengeRepository;

    public InitiateAuthenticationUseCase(ChallengeGenerationPort challengeGenerator, Clock clock, long challengeTtl, ChallengeRepository challengeRepository) {
        this.clock = clock;
        this.challengeTtl = challengeTtl;
        this.challengeGenerator = challengeGenerator;
        this.challengeRepository = challengeRepository;
    }

    Challenge handle(InitiateAuthenticationCommand command) {
        var challenge = challengeGenerator.generate(clock, challengeTtl);

        challengeRepository.save(challenge);

        return challenge;
    }
}
