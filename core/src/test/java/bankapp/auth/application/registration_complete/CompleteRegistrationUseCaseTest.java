package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.registration_complete.port.in.CompleteRegistrationCommand;
import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.LoggerPort;
import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * Using mocks instead of stubs for learning purposes.
 */
class CompleteRegistrationUseCaseTest {

    private final UUID sessionId = UUID.randomUUID();
    private final AuthSession testAuthSession = new AuthSession(
                sessionId,
                new byte[]{},
                UUID.randomUUID(),
                5L,
                Clock.systemUTC()
        );
    CredentialRecord stubCredentialRecord;
    User testUser;

    private SessionRepository sessionRepo;
    private CredentialRepository credentialRepository;
    private UserRepository userRepository;
    private WebAuthnPort webAuthnPort;
    private TokenIssuingPort tokenIssuingPort;

    private CompleteRegistrationCommand command;
    private CompleteRegistrationUseCase useCase;

    @BeforeEach
    void setUp() {
        sessionRepo = mock(SessionRepository.class);
        webAuthnPort = mock(WebAuthnPort.class);
        credentialRepository = mock(CredentialRepository.class);
        userRepository = mock(UserRepository.class);
        tokenIssuingPort = mock(TokenIssuingPort.class);

        LoggerPort log = mock(LoggerPort.class);

        command = new CompleteRegistrationCommand(sessionId, "blob");
        useCase = new CompleteRegistrationUseCase(sessionRepo, webAuthnPort, credentialRepository, userRepository, tokenIssuingPort, log);


        when(sessionRepo.load(sessionId)).thenReturn(Optional.of(testAuthSession));

        testUser = new User(new EmailAddress("test@bankapp.online"));
        var userHandle = ByteArrayUtil.uuidToBytes(testUser.getId());
        stubCredentialRecord = new CredentialRecord(
                null,
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

        when(webAuthnPort.confirmRegistrationChallenge(eq(command.RegistrationResponseJSON()), any())).thenReturn(stubCredentialRecord);
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
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
        verify(webAuthnPort).confirmRegistrationChallenge(eq(command.RegistrationResponseJSON()), eq(testAuthSession) );
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
        when(webAuthnPort.confirmRegistrationChallenge(any(),any())).thenThrow(new RuntimeException(exceptionMsg));
        // When
        // Then
       var exceptionThrowed = assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

       assertTrue(exceptionThrowed.getMessage().contains(exceptionMsg));
    }

    @Test
    void should_persist_new_credential_when_verification_successful() {

        useCase.handle(command);

        verify(credentialRepository).save(stubCredentialRecord);
    }

    @Test
    void should_delete_challenge_when_user_successfully_register_new_credential() {
        assertDoesNotThrow(() -> useCase.handle(command));
        verify(sessionRepo).delete(sessionId);
    }

    @Test
    void should_not_delete_challenge_when_user_fails_to_register_new_credential() {
        // Given
        when(webAuthnPort.confirmRegistrationChallenge(any(), any())).thenThrow(new RuntimeException("Verification failed"));

        // When & Then
        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        verify(sessionRepo, never()).delete(sessionId);
    }

    @Test
    void should_not_delete_session_when_credential_save_fails() {
        // Given
        doThrow(new RuntimeException("Database error")).when(credentialRepository).save(any());

        // When & Then
        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when credential saving fails
        verify(sessionRepo, never()).delete(sessionId);
    }

    @Test
    void should_activate_user_account_when_credential_saved_successfuly() {

        assertFalse(testUser.isEnabled());
        useCase.handle(command);
        verify(userRepository).save(argThat(User::isEnabled));
    }

    @Test
    void should_throw_exception_when_user_with_given_id_not_found() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        var exception = assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));
        assertTrue(exception.getMessage().contains("User does not exists"));
    }

    @Test
    void should_return_tokens_for_activated_user() {

        AuthTokens testTokens;
        testTokens = new AuthTokens("accessToken", "refreshToken");
        when(tokenIssuingPort.issueTokensForUser(any())).thenReturn(testTokens);

        var res = useCase.handle(command);
        verify(tokenIssuingPort).issueTokensForUser(eq(testUser.getId()));
        assertNotNull(res.tokens());
        assertNotNull(res.tokens().accessToken());
        assertNotNull(res.tokens().refreshToken());
    }

    @Test
    void should_throw_exception_when_duplicated_credential() {
        doThrow(new CredentialAlreadyExistsException("Credential already exists")).when(credentialRepository).save(any());

        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));
    }
}
//Credential already exists: What if a credential with the same public key or ID is somehow saved twice? A unique constraint in the database is essential, but the application could also check for this explicitly if needed.
