package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.infrastructure.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ChallengeGenerationService.class,
        SecurityConfiguration.class
})
class ChallengeGenerationServiceTest {

    @Autowired
    ChallengeGenerationService challengeGenerationService;

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

    @Test
    void generate_should_return_challenge_with_unique_sessionId() {

        var res = challengeGenerationService.generate();
        var res2 = challengeGenerationService.generate();

        assertNotEquals(res.sessionId(),res2.sessionId());
    }

    @Test
    void generate_should_return_challenge_with_unique_value() {

        var res = challengeGenerationService.generate();
        var res2 = challengeGenerationService.generate();

        assertFalse(Arrays.equals(res.value(), res2.value()));
    }

}