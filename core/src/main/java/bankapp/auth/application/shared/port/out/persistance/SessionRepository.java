package bankapp.auth.application.shared.port.out.persistance;


import bankapp.auth.application.shared.port.out.dto.Session;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    void save(Session session);

    Optional<Session> load(UUID key);
    void delete(UUID sessionId);
}
