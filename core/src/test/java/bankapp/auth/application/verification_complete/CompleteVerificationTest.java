package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompleteVerificationTest extends CompleteVerificationBaseTest {

    @Test
    void should_load_correct_otp_from_repository() {
        // When
        var otpOptional = otpRepository.load(DEFAULT_OTP_KEY);

        // Then
        assertThat(otpOptional).isPresent();
        assertThat(otpOptional.get().getKey()).isEqualTo(DEFAULT_OTP_KEY);
        assertThat(otpOptional.get().getValue()).isEqualTo(hashedOtpValue);
    }

    @Test
    void should_succeed_when_otp_is_valid_and_matches() {
        // When / Then
        assertDoesNotThrow(() -> defaultUseCase.handle(defaultCommand));
    }

    @Test
    void should_check_if_user_with_given_email_exists() {
        // Given
        UserRepository userRepositoryMock = mock(UserRepository.class);
        var useCase = new CompleteVerificationUseCase(challengeRepository, passkeyRepository, userRepositoryMock, credentialOptionsPort, challengeGenerator, otpService);

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(userRepositoryMock).findByEmail(DEFAULT_EMAIL);
    }

    @Test
    void should_create_new_user_if_one_does_not_exist() {
        // Given
        assertThat(userRepository.findByEmail(DEFAULT_EMAIL)).isEmpty();

        // When
        defaultUseCase.handle(defaultCommand);

        // Then
        Optional<User> userOpt = userRepository.findByEmail(DEFAULT_EMAIL);
        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void should_persist_session_after_generation() {
        // Given / When
        var res = defaultUseCase.handle(defaultCommand);
        var sessionId = res.challengeId();

        // Then
        assertThat(challengeRepository.load(sessionId)).isPresent();
    }

    @Test
    void should_make_session_valid_for_defaultTtl_value_in_seconds() {
        // Given
        var useCase = new CompleteVerificationUseCase(
                challengeRepository, passkeyRepository, userRepository, credentialOptionsPort, challengeGenerator,
                otpService);
        // When
        var res = useCase.handle(defaultCommand);

        var sessionId = res.challengeId();
        var sessionOptional = challengeRepository.load(sessionId);
        assertTrue(sessionOptional.isPresent());
        Clock fixedClockBeforeExpiration = Clock.fixed(DEFAULT_CLOCK.instant().plusSeconds(DEFAULT_TTL - 1), DEFAULT_CLOCK.getZone());
        Clock fixedClockAfterExpiration = Clock.fixed(DEFAULT_CLOCK.instant().plusSeconds(DEFAULT_TTL + 1), DEFAULT_CLOCK.getZone());

        // Then
        assertTrue(sessionOptional.get().isValid(fixedClockBeforeExpiration));
        assertFalse(sessionOptional.get().isValid(fixedClockAfterExpiration));
    }
}