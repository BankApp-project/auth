package bankapp.auth.application.shared.port.out.persistance;


import bankapp.auth.application.shared.port.out.dto.AuthSession;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    void save(AuthSession authSession, UUID key);
    Optional<AuthSession> load(UUID key);
    void delete(UUID sessionId);
}
