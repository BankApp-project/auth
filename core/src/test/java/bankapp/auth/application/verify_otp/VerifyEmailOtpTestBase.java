package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.port.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.CredentialRepository;
import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.enums.AuthMode;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.CredentialOptionsService;
import bankapp.auth.domain.service.CredentialOptionsServiceImpl;
import bankapp.auth.domain.service.UserService;
import bankapp.auth.domain.service.stubs.StubChallengeGenerator;
import bankapp.auth.domain.service.stubs.StubHasher;
import bankapp.auth.domain.service.stubs.StubOtpRepository;
import bankapp.auth.domain.service.stubs.StubUserRepository;
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;

import static org.mockito.Mockito.mock;

/**
     * An abstract base class for tests related to the VerifyEmailOtpUseCase.
     * It handles the common setup of constants, dependencies, and test data.
     */
    public abstract class VerifyEmailOtpTestBase {

        // --- SHARED CONSTANTS ---
        protected static final AuthMode DEFAULT_AUTH_MODE = AuthMode.SMARTPHONE;
        protected static final String DEFAULT_RPID = "bankapp.online";
        protected static final long DEFAULT_TIMEOUT = 30000; // 30s in ms
        protected static final Clock DEFAULT_CLOCK = Clock.systemUTC();
        protected static final int DEFAULT_TTL = 98; // in seconds
        protected static final String DEFAULT_OTP_KEY = "test@bankapp.online";
        protected static final EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_OTP_KEY);
        protected static final String DEFAULT_OTP_VALUE = "123456";

        // --- SHARED DEPENDENCIES & MOCKS ---
        // Use stubs for predictable behavior and mocks for verification
        protected OtpRepository otpRepository;
        protected HashingPort hasher;
        protected UserRepository userRepository;
        protected UserService userService;
        protected CredentialOptionsService credentialOptionsService;
        protected CredentialRepository credentialRepository;
        protected ChallengeGenerationPort challengeGenerator;

        // --- SHARED TEST DATA ---
        protected String hashedOtpValue;
        protected VerifyEmailOtpCommand defaultCommand;
        protected VerifyEmailOtpUseCase defaultUseCase;

        @BeforeEach
        void setUp() {
            // Initialize dependencies for each test to ensure isolation
            otpRepository = new StubOtpRepository();
            hasher = new StubHasher();
            userRepository = new StubUserRepository();
            userService = new UserService();
            credentialOptionsService = new CredentialOptionsServiceImpl(
                    DEFAULT_AUTH_MODE,
                    DEFAULT_RPID,
                    DEFAULT_TIMEOUT
            );
            credentialRepository = mock(CredentialRepository.class);
            challengeGenerator = new StubChallengeGenerator();

            // Create and save a valid OTP
            hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
            Otp validOtp = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
            validOtp.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
            otpRepository.save(validOtp);

            // Prepare the default command and use case instance
            defaultCommand = new VerifyEmailOtpCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
            defaultUseCase = new VerifyEmailOtpUseCase(
                    DEFAULT_CLOCK,
                    otpRepository,
                    hasher,
                    userRepository,
                    userService,
                    credentialOptionsService,
                    credentialRepository,
                    challengeGenerator
            );
        }
    }