package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompleteRegistrationUseCaseTest {

    private final UUID sessionId = UUID.randomUUID();
    private final AuthSession testAuthSession = new AuthSession(
                sessionId,
                new byte[]{},
                UUID.randomUUID(),
                5L,
                Clock.systemUTC()
        );

    private SessionRepository sessionRepo;
    private WebAuthnPort webAuthnPort;

    private CompleteRegistrationCommand command;
    private CompleteRegistrationUseCase useCase;
    @BeforeEach
    void setUp() {
        sessionRepo = mock(SessionRepository.class);
        webAuthnPort = mock(WebAuthnPort.class);

        command = new CompleteRegistrationCommand(sessionId, "blob");
        useCase = new CompleteRegistrationUseCase(sessionRepo, webAuthnPort);


        when(sessionRepo.load(sessionId)).thenReturn(Optional.of(testAuthSession));
    }

    @Test
    void should_load_authSession_from_repository() {
        // When
        useCase.handle(command);

        // Then
        verify(sessionRepo).load(eq(sessionId));
    }

    @Test
    void should_verify_user_request() {
        // When
        useCase.handle(command);

        // Then
        verify(webAuthnPort).verify(eq(command.publicKeyCredentialJson()), eq(testAuthSession) );
    }

    @Test
    void should_throw_exception_when_session_not_present_for_given_id() {
        // Given
        when(sessionRepo.load(any())).thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));
    }

    @Test
    void should_throw_CompleteRegistrationException_when_challenge_verification_fails() {
        // Given
        String exceptionMsg = "Challenge verification failed";
        when(webAuthnPort.verify(any(),any())).thenThrow(new RuntimeException(exceptionMsg));
        // When
        // Then
       var exceptionThrowed = assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

       assertTrue(exceptionThrowed.getMessage().contains(exceptionMsg));
    }
}