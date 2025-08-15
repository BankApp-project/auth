package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static bankapp.auth.domain.service.ByteArrayUtil.uuidToBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyEmailOtpUseCaseRegistrationFlowTest {

    private static final String DEFAULT_AUTH_MODE = "smartphone";
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final long DEFAULT_TIMEOUT = 30000; //30s in ms
    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    private static final int DEFAULT_TTL = 98;
    private final static String DEFAULT_OTP_KEY = "test@bankapp.online";
    private static final EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_OTP_KEY);
    private final static String DEFAULT_OTP_VALUE = "123456";

    private final OtpRepository otpRepository = new StubOtpRepository();
    private final HashingPort hasher = new StubHasher();
    private final UserRepository userRepository = new StubUserRepository();
    private final UserService userService = new UserService();
    private final ChallengeGenerationPort challengeGenerator = new StubChallengeGenerator();

    private VerifyEmailOtpCommand defaultCommand;
    private VerifyEmailOtpUseCase defaultUseCase;

    @BeforeEach
    void setUp() {
        String hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp VALID_OTP = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
        VALID_OTP.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        defaultCommand = new VerifyEmailOtpCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new VerifyEmailOtpUseCase(DEFAULT_AUTH_MODE, DEFAULT_RPID, DEFAULT_TIMEOUT, DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService, challengeGenerator);
    }

    @Test
    void should_return_Response_with_PublicKeyCredentialCreationOptions_if_user_does_not_exists() {
        // Given & When
        VerifyEmailOtpResponse res = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(RegistrationResponse.class, res);
    }

    @Test
    void should_return_Response_with_PublicKeyCredentialCreationOptions_if_users_account_is_not_enabled() {
        User user = new User(DEFAULT_EMAIL);
        userRepository.save(user);

        var res = defaultUseCase.handle(defaultCommand);

        assertInstanceOf(RegistrationResponse.class, res);
    }

    @Test
    void should_return_RegistrationResponse_with_userId_as_userHandle_if_user_does_not_exists() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);
        UserService userService = mock(UserService.class);
        VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(DEFAULT_RPID, DEFAULT_RPID, DEFAULT_TIMEOUT, DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService, challengeGenerator);
        when(userService.createUser(DEFAULT_EMAIL)).thenReturn(testUser);
        // When
        var res = useCase.handle(defaultCommand);
        RegistrationResponse registrationRes = getRegistrationResponse(res);
        byte[] userHandle = registrationRes.options().user().id();
        assertArrayEquals(userHandle, uuidToBytes(testUser.getId()));
    }

    @Test
    void should_return_RegistrationResponse_with_at_least_16bytes_long_challenge_if_user_does_not_exists_yet() {
        // Given & When
        VerifyEmailOtpResponse res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationRes = getRegistrationResponse(res);

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

        RegistrationResponse registrationRes = getRegistrationResponse(res);
        RegistrationResponse registrationRes2 = getRegistrationResponse(res2);

        byte[] challenge = registrationRes.options().challenge();
        byte[] challenge2 = registrationRes2.options().challenge();
        assertNotEquals(challenge2, challenge, "Challenges should be unique");
    }

    @Test
    void should_return_RegistrationResponse_with_email_as_userEntity_name_and_displayName_when_user_does_not_exists_yet() {
        var res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationRes = getRegistrationResponse(res);

        String name = registrationRes.options().user().name();
        String displayName = registrationRes.options().user().displayName();

        assertEquals(DEFAULT_EMAIL.getValue(), name);
        assertEquals(DEFAULT_EMAIL.getValue(), displayName);
    }

    @Test
    void should_return_RegistrationResponse_with_valid_rpId_when_user_does_not_exists_yet() {
        var res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationResponse = getRegistrationResponse(res);

        String rpId = registrationResponse.options().rp().id();

        assertEquals(DEFAULT_RPID, rpId);
    }

    @Test
    void should_return_RegistrationResponse_with_valid_PublicKeyCredentialParameters_when_user_does_not_exists_yet() {
        var res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationResponse = getRegistrationResponse(res);

        var pubKeyCredParams = registrationResponse.options().pubKeyCredParams();

        //check for "public-key" type
        assertTrue(pubKeyCredParams.stream()
                .allMatch(param -> param.type().equals("public-key")));

        //check for ES256 alg
        assertTrue(pubKeyCredParams.stream()
                .anyMatch(param -> param.alg() == -7));

        //check for RS256 alg
        assertTrue(pubKeyCredParams.stream()
                .anyMatch(param -> param.alg() == -257));

    }
    
    @Test
    void should_return_RegistrationResponse_with_valid_timeout_when_user_does_not_exists_yet() {
        var res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationResponse = getRegistrationResponse(res);
        
        var timeout = registrationResponse.options().timeout();
        
        assertNotNull(timeout);
        assertEquals(DEFAULT_TIMEOUT, timeout);
    }

    @Test
    void should_return_RegistrationResponse_with_valid_and_secure_AuthenticatorSelectionCriteria_when_user_does_not_exists_yet() {
        var res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationResponse = getRegistrationResponse(res);

        var authSelCriteria = registrationResponse.options().authenticatorSelection();

        assertNotNull(authSelCriteria);
        assertTrue(authSelCriteria.requireResidentKey());
        assertEquals("required", authSelCriteria.userVerification());

    }

    @Test
    void should_return_RegistrationResponse_with_correct_settings_based_on_authViaSmartphone_flag_when_user_does_not_exists_yet() {
        var res = defaultUseCase.handle(defaultCommand);
        RegistrationResponse registrationResponse = getRegistrationResponse(res);

        var options = registrationResponse.options();
        var authAttach = options.authenticatorSelection().authenticatorAttachment();
        var hints = options.hints();

        assertEquals("hybrid", hints.getFirst());
        assertEquals("cross-platform", authAttach);
    }

    private RegistrationResponse getRegistrationResponse(VerifyEmailOtpResponse res) {
        assertThat(res).isInstanceOf(RegistrationResponse.class);
        return (RegistrationResponse) res;
    }
}
