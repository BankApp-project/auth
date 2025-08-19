package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.domain.model.AuthSession;

import java.util.HashMap;
import java.util.Optional;

public class StubSessionRepository implements SessionRepository {

    private final HashMap<String, AuthSession> sessionCache = new HashMap<>();

    @Override
    public void save(AuthSession authSession, String key) {
        sessionCache.put(key, authSession);
    }

    @Override
    public Optional<AuthSession> load(String key) {
        return Optional.ofNullable(sessionCache.get(key));
    }
}
