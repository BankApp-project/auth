package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.stubs.StubSessionRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//and this case will be done with stubs and mocks
public class CompleteAuthenticationUseCaseTest {

    private static final long DEFAULT_TTL = 90L;
   private final Clock clock = Clock.systemUTC();
   private final Instant expirationTime = Instant.now(clock).plusSeconds(DEFAULT_TTL);

   private final UUID sessionId = UUID.randomUUID();
   private final UUID userId = UUID.randomUUID();
   private final byte[] challenge = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
   private final String authenticationResponseJSON = "blob";

   private final AuthSession testSession = new AuthSession(
           sessionId,
           challenge,
           userId,
           expirationTime
   );



   private SessionRepository sessionRepo;

   private CompleteAuthenticationUseCase useCase;
   private CompleteAuthenticationCommand command;
   private WebAuthnPort webAuthnPort;

    @BeforeEach
   void setup() {
       sessionRepo = new StubSessionRepository();
       sessionRepo.save(testSession, sessionId);

       webAuthnPort = mock(WebAuthnPort.class);

       useCase = new CompleteAuthenticationUseCase(sessionRepo, webAuthnPort);
       command = new CompleteAuthenticationCommand(sessionId, authenticationResponseJSON);
   }

    @Test
    void should_load_authSession_from_repository() {
        sessionRepo = mock(SessionRepository.class);
        useCase = new CompleteAuthenticationUseCase(sessionRepo, webAuthnPort);

        when(sessionRepo.load(eq(sessionId))).thenReturn(Optional.of(testSession));

        useCase.handle(command);

        verify(sessionRepo).load(eq(sessionId));
    }

    @Test
    void should_verify_user_request() {
        // When
        useCase.handle(command);

        // Then
        verify(webAuthnPort).confirmAuthenticationChallenge(eq(command.AuthenticationResponseJSON()), eq(testSession) );
    }

    @Test
    void should_throw_exception_when_session_not_present_for_given_id() {
        // Given
        var invalidSessionId = UUID.randomUUID();
        var invalidCommand = new CompleteAuthenticationCommand(invalidSessionId, authenticationResponseJSON);
        // When
        // Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(invalidCommand));
    }

//    @Test
//    void should_throw_CompleteAuthenticationException_when_challenge_verification_fails() {
//        // Given
//        String exceptionMsg = "Challenge verification failed";
//        when(webAuthnPort.confirmAuthenticationChallenge(any(),any())).thenThrow(new RuntimeException(exceptionMsg));
//        // When
//        // Then
//        var exceptionThrowed = assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));
//
//        assertTrue(exceptionThrowed.getMessage().contains(exceptionMsg));
//    }
}