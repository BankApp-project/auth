package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChallengeGenerationServiceTest {

    @Autowired
    ChallengeGenerationService challengeGenerationService;

    @Autowired
    ChallengeProperties properties;

    Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));

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

        assertNotEquals(res.sessionId(), res2.sessionId());
    }

    @Test
    void generate_should_return_challenge_with_unique_value() {

        var res = challengeGenerationService.generate();
        var res2 = challengeGenerationService.generate();

        assertFalse(Arrays.equals(res.value(), res2.value()));
    }

    @Test
    void generate_should_return_challenge_with_32byte_long_value() {

        var res = challengeGenerationService.generate();

        assertThat(res.value()).hasSizeGreaterThanOrEqualTo(32);
    }

    @Test
    void generate_should_return_challenge_with_ttl_as_in_properties() {

        challengeGenerationService = new ChallengeGenerationService(properties,new SecureRandom(), FIXED_CLOCK);

        var res = challengeGenerationService.generate();

        var resultExpirationTime = res.expirationTime();
        var expectedExpTime = Instant.now(FIXED_CLOCK).plus(properties.ttl());

        assertEquals(expectedExpTime, resultExpirationTime);
    }

}