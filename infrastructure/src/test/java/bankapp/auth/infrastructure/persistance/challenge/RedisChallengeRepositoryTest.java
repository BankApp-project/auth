package bankapp.auth.infrastructure.persistance.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisChallengeRepositoryTest {

    @Mock
    private RedisTemplate<String, Challenge> redisTemplate;

    @Mock
    private ValueOperations<String, Challenge> valueOperations;

    private RedisChallengeRepository redisChallengeRepository;

    @Captor
    private ArgumentCaptor<Duration> durationCaptor;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        redisChallengeRepository = new RedisChallengeRepository(redisTemplate);
    }

    @Test
    void shouldSaveChallengeAndSetTtl() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // given
        var challenge = new Challenge(UUID.randomUUID(), "challenge".getBytes(), 60, clock);
        var key = "challenge:" + challenge.sessionId().toString();

        // when
        redisChallengeRepository.save(challenge);

        // then
        verify(valueOperations).set(eq(key), eq(challenge), any(Duration.class));
    }

    @Test
    void shouldLoadChallenge() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        var challenge = new Challenge(UUID.randomUUID(), "challenge".getBytes(), 60, clock);
        var key = "challenge:" + challenge.sessionId().toString();
        when(valueOperations.get(key)).thenReturn(challenge);

        // when
        var result = redisChallengeRepository.load(challenge.sessionId());

        // then
        assertThat(result).isPresent().contains(challenge);
    }

    @Test
    void shouldDeleteChallenge() {
        // given
        var sessionId = UUID.randomUUID();
        var key = "challenge:" + sessionId.toString();

        // when
        redisChallengeRepository.delete(sessionId);

        // then
        verify(redisTemplate).delete(key);
    }
}
