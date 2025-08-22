package bankapp.auth.application.shared.port.out.persistance;


import bankapp.auth.application.shared.port.out.dto.Challenge;

import java.util.Optional;
import java.util.UUID;

public interface ChallengeRepository {
    void save(Challenge challenge);
    Optional<Challenge> load(UUID key);
    void delete(UUID sessionId);
}
