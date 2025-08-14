package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.PublicKeyCredentialRequestOptions;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.StubHasher;
import bankapp.auth.domain.service.StubOtpRepository;
import bankapp.auth.domain.service.StubUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

   /*
    Test Case 2: Verify Otp

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: User is prompted with passkey creation form.

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: handler should return true. so FE can send passkey creation form.
    */

public class VerifyEmailOtpUseCaseTest {


    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    private static final int DEFAULT_TTL = 98;
    private static final String INVALID_OTP_KEY = "nonexisting@bankapp.online";
    private final static String DEFAULT_OTP_KEY = "test@bankapp.online";
    public static final EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_OTP_KEY);
    private final static String DEFAULT_OTP_VALUE = "123456";
    private String hashedOtpValue;

    private final OtpRepository otpRepository = new StubOtpRepository();
    private final HashingPort hasher = new StubHasher();
    private final UserRepository userRepository = new StubUserRepository();

    private VerifyEmailOtpCommand defaultCommand;
    private VerifyEmailOtpUseCase defaultUseCase;

    @BeforeEach
    void setUp() {
        hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp VALID_OTP = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
        VALID_OTP.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        defaultCommand = new VerifyEmailOtpCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository);
    }

    @Test
    void should_load_correct_otp_when_otp_with_valid_email_provided() {
        Otp otp = otpRepository.load(DEFAULT_OTP_KEY);
        assertThat(otp).isNotNull();
        assertThat(otp.getKey()).isEqualTo(DEFAULT_OTP_KEY);
        assertThat(otp.getValue()).isEqualTo(hashedOtpValue);
    }

    @Test
    void should_throw_exception_when_provide_non_existing_email() {
        VerifyEmailOtpCommand invalidCommand = new VerifyEmailOtpCommand(new EmailAddress(INVALID_OTP_KEY), DEFAULT_OTP_VALUE);

        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(invalidCommand));
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
        var commandWithInvalidOtp = new VerifyEmailOtpCommand(DEFAULT_EMAIL, "invalidOtp");
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(commandWithInvalidOtp));
        assertThat(exception).hasMessageContaining("Otp does not match");
    }

    @Test
    void should_not_throw_exception_when_otp_does_match() {
        assertDoesNotThrow(() -> defaultUseCase.handle(defaultCommand));
    }

    @Test
    void should_check_if_user_with_given_email_exists() {
        UserRepository userRepository = mock(UserRepository.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository);

        useCase.handle(defaultCommand);

        verify(userRepository).findByEmail(DEFAULT_EMAIL);
    }

    @Test
    void should_return_Response_with_PublicKeyCredentialRequestOptions_if_user_already_exists() {
        UserRepository userRepository = mock(UserRepository.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository);

        User defaultUser = new User();
        when(userRepository.findByEmail(DEFAULT_EMAIL)).thenReturn(Optional.of(defaultUser));

        VerifyEmailOtpResponse res = useCase.handle(defaultCommand);
        assertNotNull(res.requestOptions());
    }
}