package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

public class InitiateAuthenticationUseCase {

    ChallengeGenerationPort challengeGenerator;
    public InitiateAuthenticationUseCase(ChallengeGenerationPort challengeGenerator) {
        this.challengeGenerator = challengeGenerator;
    }

    void handle(InitiateAuthenticationCommand command) {
        challengeGenerator.generate();
    }
}
