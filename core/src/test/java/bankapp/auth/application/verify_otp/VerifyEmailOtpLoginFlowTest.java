package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.CredentialRepository;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.ByteArrayUtil;
import bankapp.auth.domain.service.CredentialOptionsService;
import bankapp.auth.domain.service.CredentialOptionsServiceImpl;
import bankapp.auth.domain.service.UserService;
import bankapp.auth.domain.service.stubs.StubChallengeGenerator;
import bankapp.auth.domain.service.stubs.StubHasher;
import bankapp.auth.domain.service.stubs.StubOtpRepository;
import bankapp.auth.domain.service.stubs.StubUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class VerifyEmailOtpLoginFlowTest {



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
    private final CredentialOptionsService credentialOptionsService = new CredentialOptionsServiceImpl(
            DEFAULT_AUTH_MODE,
            DEFAULT_RPID,
            DEFAULT_TIMEOUT,
            new StubChallengeGenerator()
    );
    private final CredentialRepository credentialRepository = mock(CredentialRepository.class);
    private final ChallengeGenerationPort challengeGenerator = new StubChallengeGenerator();

    private VerifyEmailOtpCommand defaultCommand;
    private VerifyEmailOtpUseCase defaultUseCase;

    private final User DEFAULT_USER = new User(DEFAULT_EMAIL);

    @BeforeEach
    void setUp() {
        String hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp VALID_OTP = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
        VALID_OTP.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        defaultCommand = new VerifyEmailOtpCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository, hasher, userRepository, userService, credentialOptionsService, credentialRepository, challengeGenerator);

        DEFAULT_USER.setEnabled(true);
        userRepository.save(DEFAULT_USER);
    }

    @Test
    void should_return_Response_with_PublicKeyCredentialRequestOptions_if_user_already_exists() {
        // When
        VerifyEmailOtpResponse res = defaultUseCase.handle(defaultCommand);

        // Then
        assertInstanceOf(LoginResponse.class, res);
    }

    @Test
    void should_find_all_user_credentials_and_pass_it_to_PasskeyOptionsService_when_user_already_exists() {
        // Given
        CredentialRecord credential = new CredentialRecord(
                new byte[]{1},
                ByteArrayUtil.uuidToBytes(DEFAULT_USER.getId()),
                "public-key",
                new byte[]{0},
                0L,
                false,
                false,
                false,
                null,
                null,
                null,
                null
        );
        var credentials = List.of(credential);

        CredentialRepository mockCredentialRepository = mock(CredentialRepository.class);
        CredentialOptionsService mockCredentialOptionsService = mock(CredentialOptionsService.class);

        var useCase = new VerifyEmailOtpUseCase(
                DEFAULT_CLOCK,
                otpRepository,
                hasher,
                userRepository,
                userService,
                mockCredentialOptionsService,
                mockCredentialRepository,
                challengeGenerator);

        when(mockCredentialRepository.load(DEFAULT_USER.getId())).thenReturn(credentials);

        useCase.handle(defaultCommand);

        verify(mockCredentialOptionsService).getPasskeyRequestOptions(eq(DEFAULT_USER), eq(credentials) , any());
    }

    @Test
    void should_generate_and_pass_challenge_to_PasskeyOptionsService_when_user_already_exists() {

        CredentialOptionsService mockCredentialOptionsService = mock(CredentialOptionsService.class);
        ChallengeGenerationPort mockChallengeGenerator = mock(ChallengeGenerationPort.class);
        var challenge = ByteArrayUtil.uuidToBytes(UUID.randomUUID());
        when(mockChallengeGenerator.generate()).thenReturn(challenge);
        var useCase = new VerifyEmailOtpUseCase(
                DEFAULT_CLOCK,
                otpRepository,
                hasher,
                userRepository,
                userService,
                mockCredentialOptionsService,
                credentialRepository,
                mockChallengeGenerator);

        useCase.handle(defaultCommand);

        verify(mockCredentialOptionsService).getPasskeyRequestOptions(eq(DEFAULT_USER), any(), eq(challenge));
    }
}
