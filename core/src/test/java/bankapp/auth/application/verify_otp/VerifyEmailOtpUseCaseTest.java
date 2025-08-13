package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.StubHasher;
import bankapp.auth.domain.service.StubOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerifyEmailOtpUseCaseTest {

   /*
    Test Case 2: Verify Otp

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: User is prompted with passkey creation form.

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: handler should return true. so FE can send passkey creation form.
    */

    private static final int DEFAULT_TTL = 98;
    private static final String INVALID_OTP_KEY = "nonexisting@bankapp.online";
    private final static String VALID_OTP_KEY = "test@bankapp.online";
    private final static String VALID_OTP_VALUE = "123456";

    private final OtpRepository otpRepository = new StubOtpRepository();
    private final HashingPort hasher = new StubHasher();

    private VerifyEmailOtpCommand defaultCommand;
    private VerifyEmailOtpUseCase defaultUseCase;

    @BeforeEach
    void setUp() {
        Clock standardClock = Clock.systemUTC();
        String hashedOtpValue = hasher.hashSecurely(VALID_OTP_VALUE);
        Otp VALID_OTP = new Otp(hashedOtpValue, VALID_OTP_KEY);
        VALID_OTP.setExpirationTime(standardClock, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        defaultCommand = new VerifyEmailOtpCommand(new EmailAddress(VALID_OTP_KEY), VALID_OTP_VALUE);
        defaultUseCase = new VerifyEmailOtpUseCase(standardClock, otpRepository, hasher);
    }

        @Test
    void should_load_otp_when_otp_with_valid_email_provided() {
        defaultUseCase.handle(defaultCommand);

        assertThat(otpRepository.load(VALID_OTP_KEY)).isNotNull();
    }

    @Test
    void should_throw_exception_when_provide_non_existing_email() {
        VerifyEmailOtpCommand invalidCommand = new VerifyEmailOtpCommand(new EmailAddress(INVALID_OTP_KEY), VALID_OTP_VALUE);

        var exception = assertThrows(VerifyEmailOtpException.class,() -> defaultUseCase.handle(invalidCommand));
        assertThat(exception).hasMessageContaining("No such OTP in the system");
    }


    @Test
    void should_throw_exception_when_otp_expired() {
        Clock clock = Clock.fixed(Instant.now().plusSeconds(DEFAULT_TTL + 1), ZoneId.of("Z"));
        defaultUseCase = new VerifyEmailOtpUseCase(clock, otpRepository, hasher);

        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(defaultCommand));
        assertThat(exception).hasMessageContaining("has expired");
    }

    @Test
    void should_throw_exception_when_otp_does_not_match() {
        var commandWithInvalidOtp = new VerifyEmailOtpCommand(new EmailAddress(VALID_OTP_KEY), "invalidOtp");
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(commandWithInvalidOtp));
        assertThat(exception).hasMessageContaining("Otp does not match");
    }

    @Test
    void should_not_throw_exception_when_otp_does_match() {
        assertDoesNotThrow(() -> defaultUseCase.handle(defaultCommand));
    }
}