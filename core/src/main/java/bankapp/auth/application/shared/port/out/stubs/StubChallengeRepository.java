package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.dto.Challenge;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StubChallengeRepository implements ChallengeRepository {

    private final HashMap<UUID, Challenge> sessionCache = new HashMap<>();

    @Override
    public void save(Challenge challenge) {
        sessionCache.put(challenge.challengeId(), challenge);
    }

    @Override
    public Optional<Challenge> load(UUID key) {
        return Optional.ofNullable(sessionCache.get(key));
    }

    @Override
    public void delete(UUID sessionId) {
        sessionCache.remove(sessionId);
    }
}
