package bankapp.auth.infrastructure.driven.challenge.persistance;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.infrastructure.utils.WithRedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class RedisChallengeRepositoryTest implements WithRedisContainer {

    public static final Duration TTL = Duration.ofSeconds(60);
    private final static Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));

    @Autowired
    private RedisTemplate<String, Challenge> redisTemplate;

    private RedisChallengeRepository redisChallengeRepository;


    @BeforeEach
    void setUp() {
        redisChallengeRepository = new RedisChallengeRepository(redisTemplate, FIXED_CLOCK);
        // Clean up Redis before each test
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldSaveChallengeAndSetTtl() {
        // given
        var challenge = new Challenge(UUID.randomUUID(), "challenge".getBytes(), TTL, FIXED_CLOCK, UUID.randomUUID());
        var key = "challenge:" + challenge.challengeId();

        // when
        redisChallengeRepository.save(challenge);

        // then
        var savedChallenge = redisTemplate.opsForValue().get(key);
        assertThat(savedChallenge).isEqualTo(challenge);

        var ttl = redisTemplate.getExpire(key);
        assertEquals(TTL.getSeconds(), ttl);
    }

    @Test
    void shouldLoadChallenge() {
        // given
        var challenge = new Challenge(UUID.randomUUID(), "challenge".getBytes(), TTL, FIXED_CLOCK, UUID.randomUUID());
        var key = "challenge:" + challenge.challengeId();
        redisTemplate.opsForValue().set(key, challenge);

        // when
        var result = redisChallengeRepository.load(challenge.challengeId());

        // then
        assertThat(result).isPresent().contains(challenge);
    }

    @Test
    void shouldDeleteChallenge() {
        // given
        var challenge = new Challenge(UUID.randomUUID(), "challenge".getBytes(), TTL, FIXED_CLOCK, UUID.randomUUID());
        var sessionId = challenge.challengeId();
        var key = "challenge:" + sessionId;
        redisTemplate.opsForValue().set(key, challenge);

        // when
        redisChallengeRepository.delete(sessionId);

        // then
        var result = redisTemplate.opsForValue().get(key);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnEmptyWhenLoadingNonExistentChallenge() {
        // given
        var nonExistentSessionId = UUID.randomUUID();

        // when
        var result = redisChallengeRepository.load(nonExistentSessionId);

        // then
        assertThat(result).isEmpty();
    }
}
