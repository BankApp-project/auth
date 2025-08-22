package bankapp.auth.application.shared.port.out.persistance;


import bankapp.auth.application.shared.port.out.dto.Challenge;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    void save(Challenge challenge, UUID key);
    Optional<Challenge> load(UUID key);
    void delete(UUID sessionId);
}
