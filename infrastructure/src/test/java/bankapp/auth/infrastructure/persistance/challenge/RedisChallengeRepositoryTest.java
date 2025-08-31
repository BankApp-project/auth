package bankapp.auth.infrastructure.persistance.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.infrastructure.WithRedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisChallengeRepositoryTest implements WithRedisContainer {

    @Autowired
    private RedisTemplate<String, Challenge> redisTemplate;

    @Autowired
    private RedisChallengeRepository redisChallengeRepository;

    private final Clock clock = Clock.systemUTC();

    @Test
    void shouldSaveChallengeAndSetTtl() {
        // given
        var challenge = new Challenge(UUID.randomUUID(), "challenge".getBytes(), 60, clock);
        var key = "challenge:" + challenge.sessionId().toString();

        // when
        redisChallengeRepository.save(challenge);

        // then
        var savedChallenge = redisTemplate.opsForValue().get(key);
        var ttl = redisTemplate.getExpire(key);

        assertThat(savedChallenge).isEqualTo(challenge);
        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0);
    }
}
