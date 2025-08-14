package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.StubHasher;
import bankapp.auth.domain.service.StubOtpRepository;
import bankapp.auth.domain.service.StubUserRepository;
import bankapp.auth.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

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
    private static final EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_OTP_KEY);
    private final static String DEFAULT_OTP_VALUE = "123456";
    private String hashedOtpValue;

    private final OtpRepository otpRepository = new StubOtpRepository();
    private final HashingPort hasher = new StubHasher();
    private final UserRepository userRepository = new StubUserRepository();
    private final UserService userService = new UserService();

    private VerifyEmailOtpCommand defaultCommand;
    private VerifyEmailOtpUseCase defaultUseCase;

    @BeforeEach
    void setUp() {
        hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp VALID_OTP = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
        VALID_OTP.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        defaultCommand = new VerifyEmailOtpCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService);
    }

    @Test
    void should_load_correct_otp_when_otp_with_valid_email_provided() {
        // When
        Otp otp = otpRepository.load(DEFAULT_OTP_KEY);

        // Then
        assertThat(otp).isNotNull();
        assertThat(otp.getKey()).isEqualTo(DEFAULT_OTP_KEY);
        assertThat(otp.getValue()).isEqualTo(hashedOtpValue);
    }

    @Test
    void should_throw_exception_when_provide_non_existing_email() {
        // Given
        VerifyEmailOtpCommand invalidCommand = new VerifyEmailOtpCommand(new EmailAddress(INVALID_OTP_KEY), DEFAULT_OTP_VALUE);

        // When / Then
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(invalidCommand));
        assertThat(exception).hasMessageContaining("No such OTP in the system");
    }

    @Test
    void should_throw_exception_when_otp_expired() {
        // Given
        Clock clock = Clock.fixed(Instant.now().plusSeconds(DEFAULT_TTL + 1), ZoneId.of("Z"));
        defaultUseCase = new VerifyEmailOtpUseCase(clock, otpRepository, hasher, userRepository, userService);

        // When / Then
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(defaultCommand));
        assertThat(exception).hasMessageContaining("has expired");
    }

    @Test
    void should_throw_exception_when_otp_does_not_match() {
        // Given
        var commandWithInvalidOtp = new VerifyEmailOtpCommand(DEFAULT_EMAIL, "invalidOtp");

        // When / Then
        var exception = assertThrows(VerifyEmailOtpException.class, () -> defaultUseCase.handle(commandWithInvalidOtp));
        assertThat(exception).hasMessageContaining("Otp does not match");
    }

    @Test
    void should_not_throw_exception_when_otp_does_match() {
        // When / Then
        assertDoesNotThrow(() -> defaultUseCase.handle(defaultCommand));
    }

    @Test
    void should_check_if_user_with_given_email_exists() {
        // Given
        UserRepository userRepositoryMock = mock(UserRepository.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepositoryMock, userService);

        // When
        useCase.handle(defaultCommand);

        // Then
        verify(userRepositoryMock).findByEmail(DEFAULT_EMAIL);
    }

   @Test
   void should_return_same_user_as_original_when_user_does_not_exists() {
       // Given
       assertEquals(Optional.empty(), userRepository.findByEmail(DEFAULT_EMAIL));

       // When
       defaultUseCase.handle(defaultCommand);

       // Then
       Optional<User> userOpt = userRepository.findByEmail(DEFAULT_EMAIL);
       assertTrue(userOpt.isPresent());
       assertEquals(DEFAULT_EMAIL, userOpt.get().getEmail());
   }

    @Test
    void should_return_Response_with_PublicKeyCredentialRequestOptions_if_user_already_exists() {
        // Given
        UserRepository userRepositoryMock = mock(UserRepository.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepositoryMock, userService);
        User defaultUser = new User(DEFAULT_EMAIL);
        when(userRepositoryMock.findByEmail(DEFAULT_EMAIL)).thenReturn(Optional.of(defaultUser));

        // When
        VerifyEmailOtpResponse res = useCase.handle(defaultCommand);

        // Then
        assertInstanceOf(LoginResponse.class, res);
    }

    @Test
    void should_return_Response_with_PublicKeyCredentialCreationOptions_if_user_does_not_exists() {
        // Given & When
        VerifyEmailOtpResponse res = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(RegistrationResponse.class, res);
    }

    @Test
    void should_return_Response_with_userId_as_userHandle_if_user_does_not_exists() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);
        UserService userService = mock(UserService.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService);
        when(userService.createUser(DEFAULT_EMAIL)).thenReturn(testUser);
        // When
        var res = useCase.handle(defaultCommand);

        //Then
        assertThat(res).isInstanceOf(RegistrationResponse.class);
        RegistrationResponse registrationRes = (RegistrationResponse) res;
        byte[] userHandle = registrationRes.options().user().id();
        assertArrayEquals(userHandle, uuidToBytes(testUser.getId()));
    }

    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}