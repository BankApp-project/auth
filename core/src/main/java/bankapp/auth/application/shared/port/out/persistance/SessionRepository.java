package bankapp.auth.application.shared.port.out.persistance;


import bankapp.auth.domain.model.AuthSession;

import java.util.Optional;

public interface SessionRepository {
    void save(AuthSession authSession, String key);
    Optional<AuthSession> load(String key);
}
