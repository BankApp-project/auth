package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;

import java.util.UUID;

public class CompleteRegistrationUseCase {

    private final SessionRepository sessionRepository;

    public CompleteRegistrationUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void handle(UUID sessionId) {
        sessionRepository.load(sessionId);
    }
}
