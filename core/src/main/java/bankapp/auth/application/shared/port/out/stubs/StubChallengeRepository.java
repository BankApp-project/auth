package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StubChallengeRepository implements ChallengeRepository {

    private final HashMap<UUID, Session> sessionCache = new HashMap<>();

    @Override
    public void save(Session session) {
        sessionCache.put(session.challengeId(), session);
    }

    @Override
    public Optional<Session> load(UUID key) {
        return Optional.ofNullable(sessionCache.get(key));
    }

    @Override
    public void delete(UUID sessionId) {
        sessionCache.remove(sessionId);
    }
}
