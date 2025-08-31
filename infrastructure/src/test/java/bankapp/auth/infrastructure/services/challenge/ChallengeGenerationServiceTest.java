package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChallengeGenerationServiceTest {

    @Test
    void generate_should_return_challenge() {

        ChallengeGenerationService challengeGenerationService = new ChallengeGenerationService();

        var res = challengeGenerationService.generate();

        assertNotNull(res);
        assertInstanceOf(Challenge.class,res);
    }

}