package bankapp.auth.application.authentication.complete;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.service.PasskeyVerificationPort;
import bankapp.auth.application.shared.port.out.service.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.stubs.StubSessionRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CompleteAuthenticationUseCaseTest {

    private final static long DEFAULT_TTL = 90L;
    private final static Clock DEFAULT_CLOCK = Clock.systemUTC();
    private final static Instant DEFAULT_EXPIRATION_TIME = Instant.now(DEFAULT_CLOCK).plusSeconds(DEFAULT_TTL);

    private byte[] credentialId;
    private Passkey passkey;
    private final static UUID DEFAULT_SESSION_ID = UUID.randomUUID();
    private final static UUID DEFAULT_USER_ID = UUID.randomUUID();
    private final String authenticationResponseJSON = "blob";

    private final static byte[] DEFAULT_RAW_CHALLENGE = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
    private final static Challenge DEFAULT_CHALLENGE = new Challenge(DEFAULT_RAW_CHALLENGE, DEFAULT_EXPIRATION_TIME);
    private final static Session TEST_SESSION = new Session(
            DEFAULT_SESSION_ID,
            DEFAULT_CHALLENGE,
            DEFAULT_USER_ID
    );


    private SessionRepository sessionRepo;
    private PasskeyVerificationPort passkeyVerificationPort;
    private PasskeyRepository passkeyRepository;
    private TokenIssuingPort tokenIssuingPort;

    private CompleteAuthenticationUseCase useCase;
    private CompleteAuthenticationCommand command;

    @BeforeEach
    void setup() {
        sessionRepo = new StubSessionRepository();
        sessionRepo.save(TEST_SESSION);

        passkeyVerificationPort = mock(PasskeyVerificationPort.class);
        passkeyRepository = mock(PasskeyRepository.class);

        passkey = getCredentialRecord();
        credentialId = passkey.getId();
        when(passkeyRepository.load(credentialId)).thenReturn(Optional.of(passkey));
        when(passkeyVerificationPort.handleAuthentication(
                authenticationResponseJSON,
                TEST_SESSION,
                passkey
        )).thenReturn(passkey.signCountIncrement());

        tokenIssuingPort = mock(TokenIssuingPort.class);

        useCase = new CompleteAuthenticationUseCase(sessionRepo, passkeyVerificationPort, passkeyRepository, tokenIssuingPort);
        command = new CompleteAuthenticationCommand(DEFAULT_SESSION_ID, authenticationResponseJSON, credentialId);
    }

    private Passkey getCredentialRecord() {
        var credentialId = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        return new Passkey(
                credentialId,
                DEFAULT_USER_ID,
                "public-key",
                "test-public-key".getBytes(),
                0L,
                false,
                false,
                false,
                null,
                null,
                "test-attestation-object".getBytes(),
                "test-client-data".getBytes()
        );
    }

    @Test
    void should_load_authSession_from_repository() {
        sessionRepo = mock(SessionRepository.class);
        useCase = new CompleteAuthenticationUseCase(sessionRepo, passkeyVerificationPort, passkeyRepository, tokenIssuingPort);

        when(sessionRepo.load(eq(DEFAULT_SESSION_ID))).thenReturn(Optional.of(TEST_SESSION));

        useCase.handle(command);

        verify(sessionRepo).load(eq(DEFAULT_SESSION_ID));
    }

    @Test
    void should_verify_user_request() {
        // When
        useCase.handle(command);

        // Then
        verify(passkeyVerificationPort).handleAuthentication(eq(command.AuthenticationResponseJSON()), eq(TEST_SESSION), any());
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
        String exceptionMsg = "Session verification failed";
        when(passkeyVerificationPort.handleAuthentication(any(), any(), any())).thenThrow(new RuntimeException(exceptionMsg));
        // When
        // Then
        var exceptionThrowed = assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        assertTrue(exceptionThrowed.getMessage().contains(exceptionMsg));
    }

    @Test
    void should_load_credentialRecord_for_given_data() {
        // When & Then
        useCase.handle(command);

        verify(passkeyRepository).load(eq(credentialId));
    }

    @Test
    void should_save_updated_credentialRecord() {
        // When
        var updatedCredential = passkey.signCountIncrement();
        when(passkeyVerificationPort.handleAuthentication(eq(authenticationResponseJSON), any(), eq(passkey)))
                .thenReturn(updatedCredential);
        useCase.handle(command);
        // Then
        verify(passkeyRepository).updateSignCount(eq(updatedCredential));
    }


    @Test
    void should_delete_challenge_when_successfully_saved_updated_credential() {
        var session = sessionRepo.load(DEFAULT_SESSION_ID);
        assertTrue(session.isPresent());

        assertDoesNotThrow(() -> useCase.handle(command));

        assertTrue(sessionRepo.load(DEFAULT_SESSION_ID).isEmpty());
    }

    @Test
    void should_not_delete_challenge_when_user_fails_to_register_new_credential() {
        // Given
        when(passkeyVerificationPort.handleAuthentication(any(), any(), any())).thenThrow(new RuntimeException("Verification failed"));

        // When & Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        assertTrue(sessionRepo.load(DEFAULT_SESSION_ID).isPresent());
    }

    @Test
    void should_not_delete_challenge_when_fails_to_load_credential() {
        // Given
        when(passkeyRepository.load(credentialId)).thenThrow(new RuntimeException("Failed to load credential"));

        // When & Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        assertTrue(sessionRepo.load(DEFAULT_SESSION_ID).isPresent());
    }

    @Test
    void should_issue_tokens() {
        useCase.handle(command);
        verify(tokenIssuingPort).issueTokensForUser(passkey.getUserHandle());
    }

    @Test
    void should_return_tokens() {
        AuthTokens authTokens = new AuthTokens("accessToken", "refreshToken");
        when(tokenIssuingPort.issueTokensForUser(passkey.getUserHandle())).thenReturn(authTokens);
        var res = useCase.handle(command);
        assertEquals(res.authTokens(), authTokens);
    }
}
