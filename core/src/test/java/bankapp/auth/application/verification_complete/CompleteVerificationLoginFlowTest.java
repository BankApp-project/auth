package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompleteVerificationLoginFlowTest extends CompleteVerificationBaseTest {


    private User defaultUser;

    @BeforeEach
    @Override
    void setUp() {
        // First, run the common setup from the base class
        super.setUp();

        // Then, add the specific setup for the login flow: an existing, enabled user
        defaultUser = new User(DEFAULT_EMAIL);
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
        var credential = new Passkey(
                new byte[]{1, 2, 3, 4}, // credentialId
                defaultUser.getId(),
                // type
                new byte[]{5, 6, 7, 8}, // publicKey
                1L, // signatureCount
                true, // uvInitialized
                // backupEligible
                true, // backupState
                null // attestationObject
                // clientDataJson
                // transports
                // attestationType
        );
        var credentials = List.of(credential);

        var mockCredentialRepository = mock(CredentialRepository.class);
        var mockCredentialOptionsService = mock(CredentialOptionsPort.class);
        when(mockCredentialRepository.loadForUserId(defaultUser.getId())).thenReturn(credentials);

        var useCase = new CompleteVerificationUseCase(
                challengeTtl, log, DEFAULT_CLOCK, otpRepository, challengeRepository, mockCredentialRepository, userRepository, mockCredentialOptionsService, challengeGenerator, hasher
        );

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(eq(credentials), any());
    }

    @Test
    void should_generate_and_pass_challenge_to_service_when_user_exists() {
        // Given
        var mockCredentialOptionsService = mock(CredentialOptionsPort.class);
        var mockChallengeGenerator = mock(ChallengeGenerationPort.class);

        var challenge = new Challenge(
                UUID.randomUUID(),
                new byte[]{123},
                challengeTtl,
                DEFAULT_CLOCK
        );

        when(mockChallengeGenerator.generate(DEFAULT_CLOCK, challengeTtl)).thenReturn(challenge);

        var useCase = new CompleteVerificationUseCase(
                challengeTtl, log, DEFAULT_CLOCK, otpRepository, challengeRepository, credentialRepository, userRepository, mockCredentialOptionsService, mockChallengeGenerator, hasher
        );

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(any(), eq(challenge));
    }
}