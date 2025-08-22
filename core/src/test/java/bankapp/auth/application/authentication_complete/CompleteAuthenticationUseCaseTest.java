package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.stubs.StubSessionRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//and this case will be done with stubs and mocks
public class CompleteAuthenticationUseCaseTest {

    private static final long DEFAULT_TTL = 90L;
   private final Clock clock = Clock.systemUTC();
   private final Instant expirationTime = Instant.now(clock).plusSeconds(DEFAULT_TTL);

   private CredentialRecord credentialRecord;
   private byte[] credentialId;
   private final UUID sessionId = UUID.randomUUID();
   private final UUID userId = UUID.randomUUID();
   private final byte[] challenge = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
   private final String authenticationResponseJSON = "blob";

   private final Challenge testSession = new Challenge(
           sessionId,
           challenge,
           userId,
           expirationTime
   );



    private SessionRepository sessionRepo;
    private WebAuthnPort webAuthnPort;
    private CredentialRepository credentialRepository;
    private TokenIssuingPort tokenIssuingPort;

    private CompleteAuthenticationUseCase useCase;
    private CompleteAuthenticationCommand command;

    @BeforeEach
   void setup() {
       sessionRepo = new StubSessionRepository();
       sessionRepo.save(testSession, sessionId);

       webAuthnPort = mock(WebAuthnPort.class);
       credentialRepository = mock(CredentialRepository.class);

        credentialRecord = getCredentialRecord(userId);
        credentialId = credentialRecord.getId();
        when(credentialRepository.load(credentialId)).thenReturn(credentialRecord);

        tokenIssuingPort = mock(TokenIssuingPort.class);

        useCase = new CompleteAuthenticationUseCase(sessionRepo, webAuthnPort, credentialRepository, tokenIssuingPort);
       command = new CompleteAuthenticationCommand(sessionId, authenticationResponseJSON, credentialId);
   }

    private CredentialRecord getCredentialRecord(UUID userId) {
        var userHandle = ByteArrayUtil.uuidToBytes(userId);
        var credentialId = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        return new CredentialRecord(
                credentialId,
                userHandle,
                null,
                null,
                0L,
                false,
                false,
                false,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void should_load_authSession_from_repository() {
        sessionRepo = mock(SessionRepository.class);
        useCase = new CompleteAuthenticationUseCase(sessionRepo, webAuthnPort, credentialRepository, tokenIssuingPort);

        when(sessionRepo.load(eq(sessionId))).thenReturn(Optional.of(testSession));

        useCase.handle(command);

        verify(sessionRepo).load(eq(sessionId));
    }

    @Test
    void should_verify_user_request() {
        // When
        useCase.handle(command);

        // Then
        verify(webAuthnPort).confirmAuthenticationChallenge(eq(command.AuthenticationResponseJSON()), eq(testSession), any());
    }

    @Test
    void should_throw_exception_when_session_not_present_for_given_id() {
        // Given
        var invalidSessionId = UUID.randomUUID();
        var invalidCommand = new CompleteAuthenticationCommand(invalidSessionId, authenticationResponseJSON, credentialId);
        // When
        // Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(invalidCommand));
    }

    @Test
    void should_throw_CompleteAuthenticationException_when_challenge_verification_fails() {
        // Given
        String exceptionMsg = "Challenge verification failed";
        when(webAuthnPort.confirmAuthenticationChallenge(any(),any(), any())).thenThrow(new RuntimeException(exceptionMsg));
        // When
        // Then
        var exceptionThrowed = assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        assertTrue(exceptionThrowed.getMessage().contains(exceptionMsg));
    }

    @Test
    void should_load_credentialRecord_for_given_data() {
        // When & Then
        useCase.handle(command);

        verify(credentialRepository).load(eq(credentialId));
    }

    @Test
    void should_save_updated_credentialRecord() {
        // When
        var updatedCredential = credentialRecord.signCountIncrement();
        when(webAuthnPort.confirmAuthenticationChallenge(eq(authenticationResponseJSON), any(), eq(credentialRecord)))
                .thenReturn(updatedCredential);
        useCase.handle(command);
        // Then
        verify(credentialRepository).save(eq(updatedCredential));
    }


    @Test
    void should_delete_challenge_when_successfully_saved_updated_credential() {
        var session = sessionRepo.load(sessionId);
        assertTrue(session.isPresent());

        assertDoesNotThrow(() -> useCase.handle(command));

        assertTrue(sessionRepo.load(sessionId).isEmpty());
    }

    @Test
    void should_not_delete_challenge_when_user_fails_to_register_new_credential() {
        // Given
        when(webAuthnPort.confirmAuthenticationChallenge(any(), any(), any())).thenThrow(new RuntimeException("Verification failed"));

        // When & Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        assertTrue(sessionRepo.load(sessionId).isPresent());
    }

    @Test
    void should_not_delete_challenge_when_fails_to_load_credential() {
        // Given
        when(credentialRepository.load(credentialId)).thenThrow(new RuntimeException("Failed to load credential"));

        // When & Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        assertTrue(sessionRepo.load(sessionId).isPresent());
    }

    @Test
    void should_issue_tokens() {
       useCase.handle(command);
        verify(tokenIssuingPort).issueTokensForUser(testSession.userId());
    }

    @Test
    void should_return_tokens() {
        AuthTokens authTokens = new AuthTokens("accessToken", "refreshToken");
        when(tokenIssuingPort.issueTokensForUser(testSession.userId())).thenReturn(authTokens);
        var res = useCase.handle(command);
        assertEquals(res.authTokens(), authTokens);
    }
}
