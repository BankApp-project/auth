package bankapp.auth.application.verification.complete;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification.complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification.complete.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CompleteVerificationRegistrationFlowTest extends CompleteVerificationBaseTest {

    Challenge challenge = new Challenge(
            UUID.randomUUID(),
            new byte[]{123},
            challengeTtl,
            DEFAULT_CLOCK,
            UUID.randomUUID()
    );

    @Test
    void should_return_RegistrationResponse_if_user_does_not_exist() {
        // When
        CompleteVerificationResponse response = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(RegistrationResponse.class, response);
    }

    @Test
    void should_return_RegistrationResponse_if_user_exists_but_is_not_enabled() {
        // Given
        User disabledUser = User.createNew(DEFAULT_EMAIL);
        userRepository.save(disabledUser);

        // When
        var response = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(RegistrationResponse.class, response);
    }

    @Test
    void should_return_RegistrationResponse_with_already_persisted_user_when_user_exists_but_is_not_enabled() {
        // Given
        User disabledUser = User.createNew(DEFAULT_EMAIL);
        userRepository.save(disabledUser);

        // When
        var response = defaultUseCase.handle(defaultCommand);
        assertInstanceOf(RegistrationResponse.class, response);
        var regResponse = (RegistrationResponse) response;

        // Then
        var userIdAsBytes = ByteArrayUtil.uuidToBytes(disabledUser.getId());
        var userHandle = regResponse.options().user().id();
        assertArrayEquals(userIdAsBytes, userHandle);
    }

    @Test
    void should_pass_generated_challenge_to_CredentialOptionsService_for_new_user() {
        // Given
        var mockCredentialOptionsService = mock(CredentialOptionsPort.class);
        var mockChallengeGenerator = mock(ChallengeGenerationPort.class);
        when(mockChallengeGenerator.generate(any(UUID.class))).thenReturn(challenge);

        var useCase = new CompleteVerificationUseCase(
                challengeRepository, passkeyRepository, userRepository, mockCredentialOptionsService, mockChallengeGenerator,
                otpService);

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyCreationOptions(any(User.class), eq(challenge));
    }
}