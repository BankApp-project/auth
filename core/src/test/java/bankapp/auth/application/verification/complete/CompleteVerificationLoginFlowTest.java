package bankapp.auth.application.verification.complete;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.LoginResponse;
import bankapp.auth.application.shared.port.out.repository.PasskeyRepository;
import bankapp.auth.application.shared.port.out.service.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.service.PasskeyOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CompleteVerificationLoginFlowTest extends CompleteVerificationBaseTest {


    private User defaultUser;

    @BeforeEach
    @Override
    void setUp() {
        // First, run the common setup from the base class
        super.setUp();

        // Then, add the specific setup for the login flow: an existing, enabled user
        defaultUser = User.createNew(DEFAULT_EMAIL);
        defaultUser.activate();
        userRepository.save(defaultUser);
    }

    @Test
    void should_return_LoginResponse_if_user_already_exists_and_is_enabled() {
        // When
        CompleteVerificationResponse response = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(LoginResponse.class, response);
    }

    @Test
    void should_find_and_pass_user_credentials_to_service_when_user_exists() {
        // Given
        // Create a realistic dummy Passkey for testing purposes
        var credentials = getPasskeys();

        var mockCredentialRepository = mock(PasskeyRepository.class);
        var mockCredentialOptionsService = mock(PasskeyOptionsPort.class);
        when(mockCredentialRepository.loadForUserId(defaultUser.getId())).thenReturn(credentials);

        var useCase = new CompleteVerificationUseCase(
                sessionRepository, mockCredentialRepository, userRepository, mockCredentialOptionsService, challengeGenerator,
                otpService, sessionIdGenerator);

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(eq(credentials), any());
    }

    @Test
    void should_generate_and_pass_challenge_to_service_when_user_exists() {
        // Given
        var mockCredentialOptionsService = mock(PasskeyOptionsPort.class);
        var mockChallengeGenerator = mock(ChallengeGenerationPort.class);

        var challenge = new Challenge(
                new byte[]{123},
                challengeTtl,
                DEFAULT_CLOCK
        );

        when(mockChallengeGenerator.generate()).thenReturn(challenge);

        var useCase = new CompleteVerificationUseCase(
                sessionRepository, passkeyRepository, userRepository, mockCredentialOptionsService, mockChallengeGenerator,
                otpService, sessionIdGenerator);

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(any(), argThat(s -> s.challenge().equals(challenge)));
    }

    private List<Passkey> getPasskeys() {
        var credential = new Passkey(
                UUID.randomUUID(),
                defaultUser.getId(),
                "public-key",
                new byte[]{5, 6, 7, 8}, // publicKey
                1L, // signatureCount
                true, // uvInitialized
                false, // backupEligible
                true, // backupState
                null, // transports
                null, // extensions
                "test-attestation-object".getBytes(),
                "test-client-data".getBytes()
        );
        return List.of(credential);
    }
}