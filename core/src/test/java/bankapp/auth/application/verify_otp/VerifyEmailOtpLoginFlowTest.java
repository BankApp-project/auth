package bankapp.auth.application.verify_otp;

import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.CredentialRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.service.ByteArrayUtil;
import bankapp.auth.application.verify_otp.port.out.CredentialOptionsPort;
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

public class VerifyEmailOtpLoginFlowTest extends VerifyEmailOtpTestBase {

    private User defaultUser;

    @BeforeEach
    @Override
    void setUp() {
        // First, run the common setup from the base class
        super.setUp();

        // Then, add the specific setup for the login flow: an existing, enabled user
        defaultUser = new User(DEFAULT_EMAIL);
        defaultUser.setEnabled(true);
        userRepository.save(defaultUser);
    }

    @Test
    void should_return_LoginResponse_if_user_already_exists_and_is_enabled() {
        // When
        VerifyEmailOtpResponse response = defaultUseCase.handle(defaultCommand);

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

        var useCase = new VerifyEmailOtpUseCase(
                DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService,
                mockCredentialOptionsService, mockCredentialRepository, challengeGenerator
        );

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(eq(defaultUser), eq(credentials), any());
    }

    @Test
    void should_generate_and_pass_challenge_to_service_when_user_exists() {
        // Given
        var mockCredentialOptionsService = mock(CredentialOptionsPort.class);
        var mockChallengeGenerator = mock(ChallengeGenerationPort.class);
        var challenge = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        when(mockChallengeGenerator.generate()).thenReturn(challenge);

        var useCase = new VerifyEmailOtpUseCase(
                DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService,
                mockCredentialOptionsService, credentialRepository, mockChallengeGenerator
        );

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyRequestOptions(eq(defaultUser), any(), eq(challenge));
    }
}