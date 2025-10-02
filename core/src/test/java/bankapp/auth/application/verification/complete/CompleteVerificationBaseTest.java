package bankapp.auth.application.verification.complete;

import bankapp.auth.application.shared.port.out.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.PasskeyOptionsPort;
import bankapp.auth.application.shared.port.out.SessionIdGenerationPort;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.shared.port.out.stubs.*;
import bankapp.auth.application.verification.complete.port.in.CompleteVerificationCommand;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * An abstract base class for tests related to the CompleteVerificationUseCase.
 * It handles the common setup of constants, dependencies, and test data.
 */
public abstract class CompleteVerificationBaseTest {

    // --- SHARED CONSTANTS ---
    protected static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    protected static final long DEFAULT_TTL_IN_SECONDS = 98; // in seconds
    protected static final Duration TTL = Duration.ofSeconds(DEFAULT_TTL_IN_SECONDS);
    protected static final String DEFAULT_OTP_KEY = "test@bankapp.online";
    protected static final EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_OTP_KEY);
    protected static final String DEFAULT_OTP_VALUE = "123456";
    protected static final Duration challengeTtl = Duration.ofSeconds(66); // in seconds

    // --- SHARED DEPENDENCIES & MOCKS ---
    // Use stubs for predictable behavior and mocks for verification
    protected OtpService otpService;
    protected OtpRepository otpRepository;
    protected HashingPort hasher;
    protected UserRepository userRepository;
    protected PasskeyOptionsPort passkeyOptionsPort;
    protected PasskeyRepository passkeyRepository;
    protected ChallengeGenerationPort challengeGenerator;
    protected SessionRepository sessionRepository;
    protected SessionIdGenerationPort sessionIdGenerator;

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
        passkeyOptionsPort = new StubPasskeyOptionsService();
        passkeyRepository = mock(PasskeyRepository.class);
        challengeGenerator = new StubChallengeGenerator(DEFAULT_TTL_IN_SECONDS, DEFAULT_CLOCK);
        sessionRepository = new StubSessionRepository();
        otpService = mock(OtpService.class);
        sessionIdGenerator = mock(SessionIdGenerationPort.class);

        when(sessionIdGenerator.generate()).thenReturn(UUID.randomUUID());

        // Create and save a valid OTP
        hashedOtpValue = hasher.hashSecurely(DEFAULT_OTP_VALUE);
        Otp validOtp = Otp.createNew(DEFAULT_OTP_KEY, hashedOtpValue, DEFAULT_CLOCK, TTL);
        otpRepository.save(validOtp);

        // Prepare the default command and use case instance
        defaultCommand = new CompleteVerificationCommand(DEFAULT_EMAIL, DEFAULT_OTP_VALUE);
        defaultUseCase = new CompleteVerificationUseCase(
                sessionRepository, passkeyRepository, userRepository, passkeyOptionsPort, challengeGenerator,
                otpService, sessionIdGenerator);
    }
}