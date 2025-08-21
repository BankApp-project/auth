package bankapp.auth.application.shared.port.out.persistance;


import bankapp.auth.application.shared.port.out.dto.RegistrationSession;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    void save(RegistrationSession registrationSession, UUID key);
    Optional<RegistrationSession> load(UUID key);
    void delete(UUID sessionId);
}
