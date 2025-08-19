package bankapp.auth.application.verification_complete;

import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.stubs.StubSessionRepository;
import bankapp.auth.application.shared.port.out.stubs.StubHasher;
import bankapp.auth.application.shared.port.out.stubs.StubOtpRepository;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification_complete.port.out.CredentialRepository;
import bankapp.auth.application.verification_complete.port.out.UserRepository;
import bankapp.auth.application.verification_complete.port.out.stubs.StubChallengeGenerator;
import bankapp.auth.application.verification_complete.port.out.stubs.StubCredentialOptionsService;
import bankapp.auth.application.verification_complete.port.out.stubs.StubUserRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;

import static org.mockito.Mockito.mock;

/**
 * An abstract base class for tests related to the CompleteVerificationUseCase.
 * It handles the common setup of constants, dependencies, and test data.
 */
public abstract class VerifyEmailOtpTestBase {

    // --- SHARED CONSTANTS ---
    protected static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    protected static final int DEFAULT_TTL = 98; // in seconds
    protected static final String DEFAULT_OTP_KEY = "test@bankapp.online";
    protected static final EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_OTP_KEY);
    protected static final String DEFAULT_OTP_VALUE = "123456";
    protected static final int sessionTtl = 66; // in seconds

    // --- SHARED DEPENDENCIES & MOCKS ---
    // Use stubs for predictable behavior and mocks for verification
    protected OtpRepository otpRepository;
    protected HashingPort hasher;
    protected UserRepository userRepository;
    protected CredentialOptionsPort credentialOptionsPort;
    protected CredentialRepository credentialRepository;
    protected ChallengeGenerationPort challengeGenerator;
    protected SessionRepository sessionRepository;

    // --- SHARED TEST DATA ---
    protected String hashedOtpValue;
    protected CompleteVerificationCommand defaultCommand;
    protected CompleteVerificationUseCase defaultUseCase;

    @BeforeEach
    void setUp() {
        // Initialize dependencies for each test to ensure isolation
        otpRepository = new StubOtpRepository();
        hasher = new StubHasher();
        userRepository = new StubUserRepository();
        credentialOptionsPort = new StubCredentialOptionsService();
        credentialRepository = mock(CredentialRepository.class);
        challengeGenerator = new StubChallengeGenerator();
        sessionRepository = new StubSessionRepository();

        // Create and save a valid OTP
        hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp validOtp = new Otp(hashedOtpValue, DEFAULT_OTP_KEY);
        validOtp.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(validOtp);

        // Prepare the default command and use case instance
        defaultCommand = new CompleteVerificationCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new CompleteVerificationUseCase(
                DEFAULT_CLOCK,
                otpRepository,
                hasher,
                userRepository,
                credentialOptionsPort,
                credentialRepository,
                challengeGenerator,
                sessionRepository,
                sessionTtl);
    }
}