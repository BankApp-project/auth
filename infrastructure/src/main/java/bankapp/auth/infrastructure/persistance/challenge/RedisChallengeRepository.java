package bankapp.auth.infrastructure.persistance.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
        // Implementation will go here
    }

    @Override
    public Optional<Challenge> load(UUID key) {
        return Optional.empty(); // Implementation will go here
    }

    @Override
    public void delete(UUID sessionId) {
        // Implementation will go here
    }
}
