package bankapp.auth.application.verification_complete;

import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompleteVerificationTest extends CompleteVerificationBaseTest {

    private static final String INVALID_OTP_KEY = "nonexisting@bankapp.online";

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
    void should_throw_exception_when_otp_does_not_exist() {
        // Given
        var invalidCommand = new CompleteVerificationCommand(new EmailAddress(INVALID_OTP_KEY), DEFAULT_OTP_VALUE);

        // When / Then
        var exception = assertThrows(CompleteVerificationException.class, () -> defaultUseCase.handle(invalidCommand));
        assertThat(exception).hasMessageContaining("No such OTP in the system");
    }

    @Test
    void should_throw_exception_when_otp_is_expired() {
        // Given
        Clock fixedClock = Clock.fixed(Instant.now().plusSeconds(DEFAULT_TTL + 1), ZoneId.of("Z"));
        // Re-create use case with the clock that is in the future
        var useCaseWithFutureClock = new CompleteVerificationUseCase(sessionTtl, log, fixedClock, otpRepository, challengeRepository, credentialRepository, userRepository, credentialOptionsPort, challengeGenerator, hasher);

        // When / Then
        var exception = assertThrows(CompleteVerificationException.class, () -> useCaseWithFutureClock.handle(defaultCommand));
        assertThat(exception).hasMessageContaining("has expired");
    }

    @Test
    void should_throw_exception_when_otp_value_does_not_match() {
        // Given
        var commandWithInvalidOtp = new CompleteVerificationCommand(DEFAULT_EMAIL, "invalidOtp");

        // When / Then
        var exception = assertThrows(CompleteVerificationException.class, () -> defaultUseCase.handle(commandWithInvalidOtp));
        assertThat(exception).hasMessageContaining("Otp does not match");
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
        var useCase = new CompleteVerificationUseCase(sessionTtl, log, DEFAULT_CLOCK, otpRepository, challengeRepository, credentialRepository, userRepositoryMock, credentialOptionsPort, challengeGenerator, hasher);

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
    void should_delete_otp_after_validation() {
        // Given / When
        defaultUseCase.handle(defaultCommand);

        // Then
        assertThat(otpRepository.load(DEFAULT_OTP_KEY)).isEmpty();
    }

    @Test
    void should_persist_session_after_generation() {
        // Given / When
        var res = defaultUseCase.handle(defaultCommand);
        var sessionId = res.sessionId();

        // Then
        assertThat(challengeRepository.load(sessionId)).isPresent();
    }

    @Test
    void should_make_session_valid_for_defaultTtl_value_in_seconds() {
        // Given
        long sessionTtl = 66L;
        var useCase = new CompleteVerificationUseCase(
                sessionTtl, log, DEFAULT_CLOCK,
                otpRepository,
                challengeRepository, credentialRepository, userRepository, credentialOptionsPort, challengeGenerator, hasher
        );
        // When
        var res = useCase.handle(defaultCommand);

        var sessionId = res.sessionId();
        var sessionOptional = challengeRepository.load(sessionId);
        assertTrue(sessionOptional.isPresent());
        Clock fixedClockBeforeExpiration = Clock.fixed(DEFAULT_CLOCK.instant().plusSeconds(sessionTtl-1),DEFAULT_CLOCK.getZone());
        Clock fixedClockAfterExpiration = Clock.fixed(DEFAULT_CLOCK.instant().plusSeconds(sessionTtl+1), DEFAULT_CLOCK.getZone());

        // Then
        assertTrue(sessionOptional.get().isValid(fixedClockBeforeExpiration));
        assertFalse(sessionOptional.get().isValid(fixedClockAfterExpiration));
    }
}