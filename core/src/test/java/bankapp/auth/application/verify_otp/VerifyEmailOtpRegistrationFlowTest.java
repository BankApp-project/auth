package bankapp.auth.application.verify_otp;

import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.service.ByteArrayUtil;
import bankapp.auth.application.verify_otp.port.out.CredentialOptionsPort;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VerifyEmailOtpRegistrationFlowTest extends VerifyEmailOtpTestBase {

    @Test
    void should_return_RegistrationResponse_if_user_does_not_exist() {
        // When
        VerifyEmailOtpResponse response = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(RegistrationResponse.class, response);
    }

    @Test
    void should_return_RegistrationResponse_if_user_exists_but_is_not_enabled() {
        // Given
        User disabledUser = new User(DEFAULT_EMAIL);
        disabledUser.setEnabled(false); // Explicitly set to false
        userRepository.save(disabledUser);

        // When
        var response = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(RegistrationResponse.class, response);
    }

    @Test
    void should_return_RegistrationResponse_with_already_persisted_user_when_user_exists_but_is_not_enabled() {
        // Given
        User disabledUser = new User(DEFAULT_EMAIL);
        disabledUser.setEnabled(false); // Explicitly set to false
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
        var challenge = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        when(mockChallengeGenerator.generate()).thenReturn(challenge);

        var useCase = new VerifyEmailOtpUseCase(
                DEFAULT_CLOCK, otpRepository, hasher, userRepository,
                mockCredentialOptionsService, credentialRepository, mockChallengeGenerator,
                sessionRepository);

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(mockCredentialOptionsService).getPasskeyCreationOptions(any(User.class), eq(challenge));
    }
}