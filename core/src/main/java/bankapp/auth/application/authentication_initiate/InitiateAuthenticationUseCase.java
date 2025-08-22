package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

import java.time.Clock;

public class InitiateAuthenticationUseCase {

    private final Clock clock;
    private final long challengeTtl;

    ChallengeGenerationPort challengeGenerator;
    public InitiateAuthenticationUseCase(ChallengeGenerationPort challengeGenerator, Clock clock, long challengeTtl) {
        this.challengeGenerator = challengeGenerator;
        this.clock = clock;
        this.challengeTtl = challengeTtl;
    }

    void handle(InitiateAuthenticationCommand command) {
        challengeGenerator.generate(clock, challengeTtl);
    }
}
