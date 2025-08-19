package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class CompleteRegistrationUseCaseTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void should_load_challenge_from_session() {
        // Given
        var sessionRepo = mock(SessionRepository.class);
        var useCase = new CompleteRegistrationUseCase(sessionRepo);
        var sessionId = UUID.randomUUID();

        // When
        useCase.handle(sessionId);

        // Then
        verify(sessionRepo).load(eq(sessionId));
    }
}