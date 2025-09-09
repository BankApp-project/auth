package bankapp.auth.application.registration.complete;

import bankapp.auth.application.registration.complete.port.in.CompleteRegistrationCommand;
import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompleteRegistrationUseCaseTest {

    private final static long DEFAULT_TTL = 90L;
    private final static UUID DEFAULT_CHALLENGE_ID = UUID.randomUUID();
    private final static Challenge DEFAULT_CHALLENGE = new Challenge(
            DEFAULT_CHALLENGE_ID,
            new byte[]{111},
            Duration.ofSeconds(DEFAULT_TTL),
            Clock.systemUTC()
    );
    private PasskeyRegistrationData stubRegistrationData;
    private User testUser;

    private ChallengeRepository sessionRepo;
    private PasskeyRepository passkeyRepository;
    private UserRepository userRepository;
    private WebAuthnPort webAuthnPort;
    private TokenIssuingPort tokenIssuingPort;

    private CompleteRegistrationCommand command;
    private CompleteRegistrationUseCase useCase;

    @BeforeEach
    void setUp() {
        sessionRepo = mock(ChallengeRepository.class);
        webAuthnPort = mock(WebAuthnPort.class);
        passkeyRepository = mock(PasskeyRepository.class);
        userRepository = mock(UserRepository.class);
        tokenIssuingPort = mock(TokenIssuingPort.class);

        command = new CompleteRegistrationCommand(DEFAULT_CHALLENGE_ID, "blob");
        useCase = new CompleteRegistrationUseCase(sessionRepo, webAuthnPort, passkeyRepository, userRepository, tokenIssuingPort);


        when(sessionRepo.load(DEFAULT_CHALLENGE_ID)).thenReturn(Optional.of(DEFAULT_CHALLENGE));

        testUser = User.createNew(new EmailAddress("test@bankapp.online"));
        stubRegistrationData = new PasskeyRegistrationData(
                UUID.randomUUID(),
                testUser.getId(),
                "public-key",
                "public key array".getBytes(),
                0L,
                false,
                false,
                false,
                null,
                null,
                "attestationObject".getBytes(),
                "attestationClientData".getBytes()
        );


        when(webAuthnPort.confirmRegistrationChallenge(eq(command.RegistrationResponseJSON()), any())).thenReturn(stubRegistrationData);
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
    }

    @Test
    void should_load_authSession_from_repository() {
        // When
        useCase.handle(command);

        // Then
        verify(sessionRepo).load(eq(DEFAULT_CHALLENGE_ID));
    }

    @Test
    void should_verify_user_request() {
        // When
        useCase.handle(command);

        // Then
        verify(webAuthnPort).confirmRegistrationChallenge(eq(command.RegistrationResponseJSON()), eq(DEFAULT_CHALLENGE));
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
        when(webAuthnPort.confirmRegistrationChallenge(any(), any())).thenThrow(new RuntimeException(exceptionMsg));
        // When
        // Then
        var exceptionThrowed = assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

        assertTrue(exceptionThrowed.getMessage().contains(exceptionMsg));
    }

    @Test
    void should_persist_new_credential_when_verification_successful() {

        useCase.handle(command);

        verify(passkeyRepository).save(stubRegistrationData);
    }

    @Test
    void should_delete_challenge_when_user_successfully_register_new_credential() {
        assertDoesNotThrow(() -> useCase.handle(command));
        verify(sessionRepo).delete(DEFAULT_CHALLENGE_ID);
    }

    @Test
    void should_not_delete_challenge_when_user_fails_to_register_new_credential() {
        // Given
        when(webAuthnPort.confirmRegistrationChallenge(any(), any())).thenThrow(new RuntimeException("Verification failed"));

        // When & Then
        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when verification fails
        verify(sessionRepo, never()).delete(DEFAULT_CHALLENGE_ID);
    }

    @Test
    void should_not_delete_session_when_credential_save_fails() {
        // Given
        doThrow(new RuntimeException("Database error")).when(passkeyRepository).save(any());

        // When & Then
        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));

        // Verify session is NOT deleted when credential saving fails
        verify(sessionRepo, never()).delete(DEFAULT_CHALLENGE_ID);
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
        assertNotNull(res.authTokens());
        assertNotNull(res.authTokens().accessToken());
        assertNotNull(res.authTokens().refreshToken());
    }

    @Test
    void should_throw_exception_when_duplicated_credential() {
        doThrow(new CredentialAlreadyExistsException("Credential already exists")).when(passkeyRepository).save(any());

        assertThrows(CompleteRegistrationException.class, () -> useCase.handle(command));
    }
}