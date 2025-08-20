package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;

public class CompleteAuthenticationUseCase {

    private SessionRepository sessionRepository;

    public CompleteAuthenticationUseCase(SessionRepository sessionRepo) {
        this.sessionRepository = sessionRepo;
    }

    public CompleteAuthenticationResponse handle(CompleteAuthenticationCommand command) {
        sessionRepository.load(command.sessionId());

        return null;
    }
}
