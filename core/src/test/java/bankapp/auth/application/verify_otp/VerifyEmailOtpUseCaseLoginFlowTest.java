package bankapp.auth.application.verify_otp;

import bankapp.auth.application.initiate_verification.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyEmailOtpUseCaseLoginFlowTest {


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
        defaultUseCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService, challengeGenerator);
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
    void should_return_LoginResponse_with_at_least_16bytes_long_challenge_if_user_already_exists() {
        //persist defaultUser pre useCase handling
        User user = new User(DEFAULT_EMAIL);
        userRepository.save(user);

        var res = defaultUseCase.handle(defaultCommand);

        assertThat(res).isInstanceOf(LoginResponse.class);
        LoginResponse loginRes = (LoginResponse) res;
        byte[] challenge = loginRes.options().challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
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
}
