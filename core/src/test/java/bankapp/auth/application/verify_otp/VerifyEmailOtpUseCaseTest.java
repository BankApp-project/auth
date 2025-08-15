package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private final ChallengeGenerationPort challengeGenerator = new StubChallengeGenerator();

    private VerifyEmailOtpCommand defaultCommand;
    private VerifyEmailOtpUseCase defaultUseCase;

    @BeforeEach
    void setUp() {
        hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp VALID_OTP = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
        VALID_OTP.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        defaultCommand = new VerifyEmailOtpCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService, challengeGenerator);
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
        defaultUseCase = new VerifyEmailOtpUseCase(clock, otpRepository, hasher, userRepository, userService, challengeGenerator);

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
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepositoryMock, userService, challengeGenerator);

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
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepositoryMock, userService, challengeGenerator);
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
    void should_return_RegistrationResponse_with_userId_as_userHandle_if_user_does_not_exists() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);
        UserService userService = mock(UserService.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService, challengeGenerator);
        when(userService.createUser(DEFAULT_EMAIL)).thenReturn(testUser);
        // When
        var res = useCase.handle(defaultCommand);

        //Then
        assertThat(res).isInstanceOf(RegistrationResponse.class);
        RegistrationResponse registrationRes = (RegistrationResponse) res;
        byte[] userHandle = registrationRes.options().user().id();
        assertArrayEquals(userHandle, uuidToBytes(testUser.getId()));
    }

    @Test
    void should_return_RegistrationResponse_with_at_least_16bytes_long_challenge_if_user_does_not_exists_yet() {
        // Given & When
        VerifyEmailOtpResponse res = defaultUseCase.handle(defaultCommand);

        // Then
        assertThat(res).isInstanceOf(RegistrationResponse.class);
        RegistrationResponse registrationRes = (RegistrationResponse) res;
        byte[] challenge = registrationRes.options().challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_RegistrationResponse_with_unique_Challenge_if_user_does_not_exists_yet() {
        // First attempt
        var res = defaultUseCase.handle(defaultCommand);

        // Prepare data for second attempt - create another OTP entry
        String secondEmail = "test2@bankapp.online";
        String secondOtpValue = "654321";
        EmailAddress secondEmailAddress = new EmailAddress(secondEmail);
        String hashedSecondOtpValue = hasher.hashSecurely(secondOtpValue);

        // Save second OTP in database
        Otp secondValidOtp = new Otp(hashedSecondOtpValue, secondEmail);
        secondValidOtp.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(secondValidOtp);

        // Create second command
        VerifyEmailOtpCommand command2 = new VerifyEmailOtpCommand(secondEmailAddress, secondOtpValue);

        // Second attempt
        var res2 = defaultUseCase.handle(command2);

        assertThat(res).isInstanceOf(RegistrationResponse.class);
        assertThat(res2).isInstanceOf(RegistrationResponse.class);

        RegistrationResponse registrationRes = (RegistrationResponse) res;
        RegistrationResponse registrationRes2 = (RegistrationResponse) res2; // Fixed: was assigning res instead of res2

        byte[] challenge = registrationRes.options().challenge();
        byte[] challenge2 = registrationRes2.options().challenge();
        assertNotEquals(challenge2, challenge, "Challenges should be unique");
    }

    @Test
    void should_return_unique_LoginResponse_if_user_already_exists() {
        // Given - Create and save two existing users using stub
        User firstUser = new User(DEFAULT_EMAIL);
        userRepository.save(firstUser);

        // Prepare data for second user - create another OTP entry
        String secondEmail = "test2@bankapp.online";
        String secondOtpValue = "654321";
        EmailAddress secondEmailAddress = new EmailAddress(secondEmail);
        String hashedSecondOtpValue = hasher.hashSecurely(secondOtpValue);

        // Save second OTP in database
        Otp secondValidOtp = new Otp(hashedSecondOtpValue, secondEmail);
        secondValidOtp.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(secondValidOtp);

        User secondUser = new User(secondEmailAddress);
        userRepository.save(secondUser);

        // Create second command
        VerifyEmailOtpCommand command2 = new VerifyEmailOtpCommand(secondEmailAddress, secondOtpValue);

        // When - Handle both commands
        VerifyEmailOtpResponse res1 = defaultUseCase.handle(defaultCommand);
        VerifyEmailOtpResponse res2 = defaultUseCase.handle(command2);

        // Then - Both should be LoginResponse instances with unique challenges
        assertThat(res1).isInstanceOf(LoginResponse.class);
        assertThat(res2).isInstanceOf(LoginResponse.class);

        LoginResponse loginRes1 = (LoginResponse) res1;
        LoginResponse loginRes2 = (LoginResponse) res2;

        byte[] challenge1 = loginRes1.options().challenge();
        byte[] challenge2 = loginRes2.options().challenge();

        assertThat(challenge1).isNotEqualTo(challenge2);
    }

    private static byte[] uuidToBytes(UUID uuid) {
        return ByteArrayUtil.uuidToBytes(uuid);
    }

   /*
1.  A request hits your **Controller**.
2.  The Controller calls an **Application Service** in your core domain (e.g., `AuthenticationService`).
3.  The **Application Service** performs the business logic:
    *   It fetches the `User` from a `UserRepository`.
    *   It calls a `ChallengeGenerator` port to get a new, secure challenge.
    *   It maps the user's registered keys into a list of `PublicKeyCredentialDescriptor` DTOs.
    *   It assembles all of this into your `domain.dto.PublicKeyCredentialRequestOptions` object.
4.  The Application Service returns this DTO.
5.  The Controller serializes the DTO to JSON and sends it to the client.
    */
}