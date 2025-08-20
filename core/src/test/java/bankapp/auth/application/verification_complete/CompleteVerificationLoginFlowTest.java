package bankapp.auth.application.verification_complete;

import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.persistance.CredentialRepository;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.service.ByteArrayUtil;
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
        // Create a realistic dummy CredentialRecord for testing purposes
        var credential = new CredentialRecord(
                new byte[]{1, 2, 3, 4}, // credentialId
                ByteArrayUtil.uuidToBytes(defaultUser.getId()), // userId
                "public-key", // type
                new byte[]{5, 6, 7, 8}, // publicKey
                1L, // signatureCount
                true, // uvInitialized
                true, // backupEligible
                true, // backupState
                null, // attestationObject
                null, // clientDataJson
                null, // transports
                null  // attestationType
        );
        var credentials = List.of(credential);

        var mockCredentialRepository = mock(CredentialRepository.class);
        var mockCredentialOptionsService = mock(CredentialOptionsPort.class);
        when(mockCredentialRepository.load(defaultUser.getId())).thenReturn(credentials);

        var useCase = new CompleteVerificationUseCase(
                sessionTtl, log, DEFAULT_CLOCK, otpRepository, sessionRepository, mockCredentialRepository, userRepository, mockCredentialOptionsService, challengeGenerator, hasher
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
        var challenge = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        when(mockChallengeGenerator.generate()).thenReturn(challenge);

        var useCase = new CompleteVerificationUseCase(
                sessionTtl, log, DEFAULT_CLOCK, otpRepository, sessionRepository, credentialRepository, userRepository, mockCredentialOptionsService, mockChallengeGenerator, hasher
        );

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(any(), eq(challenge));
    }
}