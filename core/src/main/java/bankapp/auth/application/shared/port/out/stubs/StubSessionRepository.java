package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.dto.RegistrationSession;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StubSessionRepository implements SessionRepository {

    private final HashMap<UUID, RegistrationSession> sessionCache = new HashMap<>();

    @Override
    public void save(RegistrationSession registrationSession, UUID key) {
        sessionCache.put(key, registrationSession);
    }

    @Override
    public Optional<RegistrationSession> load(UUID key) {
        return Optional.ofNullable(sessionCache.get(key));
    }

    @Override
    public void delete(UUID sessionId) {
        sessionCache.remove(sessionId);
    }
}
