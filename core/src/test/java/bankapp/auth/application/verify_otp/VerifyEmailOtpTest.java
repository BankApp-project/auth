package bankapp.auth.application.verify_otp;

import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.Otp;
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

public class VerifyEmailOtpTest extends VerifyEmailOtpTestBase {

    private static final String INVALID_OTP_KEY = "nonexisting@bankapp.online";

    @Test
    void should_load_correct_otp_from_repository() {
        // When
        Otp otp = otpRepository.load(DEFAULT_OTP_KEY);

        // Then
        assertThat(otp).isNotNull();
        assertThat(otp.getKey()).isEqualTo(DEFAULT_OTP_KEY);
        assertThat(otp.getValue()).isEqualTo(hashedOtpValue);
    }

    @Test
    void should_throw_exception_when_otp_does_not_exist() {
        // Given
        var invalidCommand = new VerifyEmailOtpCommand(new EmailAddress(INVALID_OTP_KEY), DEFAULT_OTP_VALUE);

        // When / Then
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(invalidCommand));
        assertThat(exception).hasMessageContaining("No such OTP in the system");
    }

    @Test
    void should_throw_exception_when_otp_is_expired() {
        // Given
        Clock fixedClock = Clock.fixed(Instant.now().plusSeconds(DEFAULT_TTL + 1), ZoneId.of("Z"));
        // Re-create use case with the clock that is in the future
        var useCaseWithFutureClock = new VerifyEmailOtpUseCase(fixedClock, otpRepository, hasher, userRepository, userService, credentialOptionsService, credentialRepository, challengeGenerator);

        // When / Then
        var exception = assertThrows(VerifyEmailOtpException.class, () -> useCaseWithFutureClock.handle(defaultCommand));
        assertThat(exception).hasMessageContaining("has expired");
    }

    @Test
    void should_throw_exception_when_otp_value_does_not_match() {
        // Given
        var commandWithInvalidOtp = new VerifyEmailOtpCommand(DEFAULT_EMAIL, "invalidOtp");

        // When / Then
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(commandWithInvalidOtp));
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
        var useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepositoryMock, userService, credentialOptionsService, credentialRepository, challengeGenerator);

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
}