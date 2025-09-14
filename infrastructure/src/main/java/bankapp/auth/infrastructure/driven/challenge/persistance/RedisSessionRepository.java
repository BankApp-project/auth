package bankapp.auth.infrastructure.driven.challenge.persistance;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisSessionRepository implements SessionRepository {

    private static final String KEY_PREFIX = "session:";

    private final RedisTemplate<String, Session> redisTemplate;
    private final Clock clock;

    @Override
    public void save(Session session) {
        var key = KEY_PREFIX + session.sessionId().toString();
        var timeout = Duration.between(Instant.now(clock), session.challenge().expirationTime());
        redisTemplate.opsForValue().set(key, session, timeout);
    }

    @Override
    public Optional<Session> load(UUID key) {
        var redisKey = KEY_PREFIX + key.toString();
        return Optional.ofNullable(redisTemplate.opsForValue().get(redisKey));
    }

    @Override
    public void delete(UUID sessionId) {
        var redisKey = KEY_PREFIX + sessionId.toString();
        redisTemplate.delete(redisKey);
    }
}
