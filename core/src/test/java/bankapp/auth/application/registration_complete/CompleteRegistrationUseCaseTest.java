package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verification_complete.port.out.CredentialRepository;
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
    private CredentialRepository credentialRepository;

    private CompleteRegistrationCommand command;
    private CompleteRegistrationUseCase useCase;

    @BeforeEach
    void setUp() {
        sessionRepo = mock(SessionRepository.class);
        webAuthnPort = mock(WebAuthnPort.class);
        credentialRepository = mock(CredentialRepository.class);

        command = new CompleteRegistrationCommand(sessionId, "blob");
        useCase = new CompleteRegistrationUseCase(sessionRepo, webAuthnPort, credentialRepository);


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
        verify(webAuthnPort).confirmRegistrationChallenge(eq(command.publicKeyCredentialJson()), eq(testAuthSession) );
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

        CredentialRecord stubCredentialRecord = new CredentialRecord(
                null,
                null,
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

        when(webAuthnPort.confirmRegistrationChallenge(eq(command.publicKeyCredentialJson()), any())).thenReturn(stubCredentialRecord);

        useCase.handle(command);

        verify(credentialRepository).save(stubCredentialRecord);
    }
}