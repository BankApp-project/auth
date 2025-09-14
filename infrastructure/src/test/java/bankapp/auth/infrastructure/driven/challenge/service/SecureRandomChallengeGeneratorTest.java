package bankapp.auth.infrastructure.driven.challenge.service;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.infrastructure.driven.challenge.config.ChallengeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecureRandomChallengeGeneratorTest {

    @Autowired
    SecureRandomChallengeGenerator secureRandomChallengeGenerator;

    @Autowired
    ChallengeProperties properties;

    Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));

    @Test
    void generate_should_return_challenge() {

        var res = secureRandomChallengeGenerator.generate(UUID.randomUUID());

        assertNotNull(res);
        assertInstanceOf(Session.class, res);
    }

    @Test
    void generate_should_return_challenge_with_not_null_values() {

        var res = secureRandomChallengeGenerator.generate(UUID.randomUUID());

        assertThat(res)
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void generate_should_return_challenge_with_unique_sessionId() {

        var res = secureRandomChallengeGenerator.generate(UUID.randomUUID());
        var res2 = secureRandomChallengeGenerator.generate(UUID.randomUUID());

        assertNotEquals(res.sessionId(), res2.sessionId());
    }

    @Test
    void generate_should_return_challenge_with_unique_value() {

        var res = secureRandomChallengeGenerator.generate(UUID.randomUUID());
        var res2 = secureRandomChallengeGenerator.generate(UUID.randomUUID());

        assertFalse(Arrays.equals(res.challenge(), res2.challenge()));
    }

    @Test
    void generate_should_return_challenge_with_32byte_long_value() {

        var res = secureRandomChallengeGenerator.generate(UUID.randomUUID());

        assertThat(res.challenge()).hasSizeGreaterThanOrEqualTo(32);
    }

    @Test
    void generate_should_return_challenge_with_ttl_as_in_properties() {

        secureRandomChallengeGenerator = new SecureRandomChallengeGenerator(properties,new SecureRandom(), FIXED_CLOCK);

        var res = secureRandomChallengeGenerator.generate(UUID.randomUUID());

        var resultExpirationTime = res.expirationTime();
        var expectedExpTime = Instant.now(FIXED_CLOCK).plus(properties.ttl());

        assertEquals(expectedExpTime, resultExpirationTime);
    }

}