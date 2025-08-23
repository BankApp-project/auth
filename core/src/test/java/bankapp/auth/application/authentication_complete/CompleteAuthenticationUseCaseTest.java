package bankapp.auth.application.authentication_complete;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.stubs.StubChallengeRepository;
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
    private final static Challenge TEST_CHALLENGE = new Challenge(
            DEFAULT_SESSION_ID,
            DEFAULT_RAW_CHALLENGE,
            DEFAULT_EXPIRATION_TIME
    );


    private ChallengeRepository sessionRepo;
    private WebAuthnPort webAuthnPort;
    private CredentialRepository credentialRepository;
    private TokenIssuingPort tokenIssuingPort;

    private CompleteAuthenticationUseCase useCase;
    private CompleteAuthenticationCommand command;

    @BeforeEach
    void setup() {
        sessionRepo = new StubChallengeRepository();
        sessionRepo.save(TEST_CHALLENGE);

        webAuthnPort = mock(WebAuthnPort.class);
        credentialRepository = mock(CredentialRepository.class);

        passkey = getCredentialRecord();
        credentialId = passkey.getId();
        when(credentialRepository.load(credentialId)).thenReturn(passkey);
        when(webAuthnPort.confirmAuthenticationChallenge(
                authenticationResponseJSON,
                TEST_CHALLENGE,
                passkey
        )).thenReturn(passkey.signCountIncrement());

        tokenIssuingPort = mock(TokenIssuingPort.class);

        useCase = new CompleteAuthenticationUseCase(sessionRepo, webAuthnPort, credentialRepository, tokenIssuingPort);
        command = new CompleteAuthenticationCommand(DEFAULT_SESSION_ID, authenticationResponseJSON, credentialId);
    }

    private Passkey getCredentialRecord() {
        var credentialId = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        return new Passkey(
                credentialId,
                DEFAULT_USER_ID,
                null,
                0L,
                false,
                false,
                null
        );
    }

    @Test
    void should_load_authSession_from_repository() {
        sessionRepo = mock(ChallengeRepository.class);
        useCase = new CompleteAuthenticationUseCase(sessionRepo, webAuthnPort, credentialRepository, tokenIssuingPort);

        when(sessionRepo.load(eq(DEFAULT_SESSION_ID))).thenReturn(Optional.of(TEST_CHALLENGE));

        useCase.handle(command);

        verify(sessionRepo).load(eq(DEFAULT_SESSION_ID));
    }

    @Test
    void should_verify_user_request() {
        // When
        useCase.handle(command);

        // Then
        verify(webAuthnPort).confirmAuthenticationChallenge(eq(command.AuthenticationResponseJSON()), eq(TEST_CHALLENGE), any());
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
        when(webAuthnPort.confirmAuthenticationChallenge(any(), any(), any())).thenThrow(new RuntimeException(exceptionMsg));
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
        var updatedCredential = passkey.signCountIncrement();
        when(webAuthnPort.confirmAuthenticationChallenge(eq(authenticationResponseJSON), any(), eq(passkey)))
                .thenReturn(updatedCredential);
        useCase.handle(command);
        // Then
        verify(credentialRepository).save(eq(updatedCredential));
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
        when(webAuthnPort.confirmAuthenticationChallenge(any(), any(), any())).thenThrow(new RuntimeException("Verification failed"));

        // When & Then
        assertThrows(CompleteAuthenticationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        assertTrue(sessionRepo.load(DEFAULT_SESSION_ID).isPresent());
    }

    @Test
    void should_not_delete_challenge_when_fails_to_load_credential() {
        // Given
        when(credentialRepository.load(credentialId)).thenThrow(new RuntimeException("Failed to load credential"));

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
