package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.dto.AuthSession;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StubSessionRepository implements SessionRepository {

    private final HashMap<UUID, AuthSession> sessionCache = new HashMap<>();

    @Override
    public void save(AuthSession authSession, UUID key) {
        sessionCache.put(key, authSession);
    }

    @Override
    public Optional<AuthSession> load(UUID key) {
        return Optional.ofNullable(sessionCache.get(key));
    }
}
