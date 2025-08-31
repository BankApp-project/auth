package bankapp.auth.infrastructure.persistance.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisChallengeRepository implements ChallengeRepository {

    private final RedisTemplate<String, Challenge> redisTemplate;
    private static final String KEY_PREFIX = "challenge:";

    public RedisChallengeRepository(RedisTemplate<String, Challenge> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(Challenge challenge) {
        var key = KEY_PREFIX + challenge.sessionId().toString();
        var timeout = Duration.between(Instant.now(), challenge.expirationTime());
        redisTemplate.opsForValue().set(key, challenge, timeout);
    }

    @Override
    public Optional<Challenge> load(UUID key) {
        var redisKey = KEY_PREFIX + key.toString();
        return Optional.ofNullable(redisTemplate.opsForValue().get(redisKey));
    }

    @Override
    public void delete(UUID sessionId) {
        var redisKey = KEY_PREFIX + sessionId.toString();
        redisTemplate.delete(redisKey);
    }
}
