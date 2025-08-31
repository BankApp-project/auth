package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChallengeGenerationServiceTest {

    ChallengeGenerationService challengeGenerationService = new ChallengeGenerationService();

    @Test
    void generate_should_return_challenge() {

        var res = challengeGenerationService.generate();

        assertNotNull(res);
        assertInstanceOf(Challenge.class, res);
    }

    @Test
    void generate_should_return_challenge_with_not_null_values() {

        var res = challengeGenerationService.generate();

        assertThat(res)
                .hasNoNullFieldsOrProperties();
    }
}