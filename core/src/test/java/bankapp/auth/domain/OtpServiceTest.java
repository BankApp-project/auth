package bankapp.auth.domain;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.service.HashingPort;
import bankapp.auth.application.shared.port.out.stubs.StubHasher;
import bankapp.auth.application.shared.port.out.stubs.StubOtpRepository;
import bankapp.auth.application.verification.complete.OtpVerificationException;
import bankapp.auth.application.verification.initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;
import bankapp.auth.domain.port.out.stubs.StubOtpConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class OtpServiceTest {

    private static final int OTP_SIZE = 6;
    private static final int TTL_IN_SECONDS = 60;
    private static final Duration TTL = Duration.ofSeconds(TTL_IN_SECONDS);
    private static final Clock CLOCK = Clock.systemUTC();
    private static final String OTP_VALUE = "123456";
    private static final String EMAIL_VALUE = "test@bankapp.online";
    private static final EmailAddress EMAIL_ADDRESS = new EmailAddress(EMAIL_VALUE);

    private final HashingPort hasher = new StubHasher();
    private final OtpConfigPort config = new StubOtpConfig(OTP_SIZE, TTL_IN_SECONDS, CLOCK);
    private final OtpRepository otpRepository = new StubOtpRepository();

    @Mock
    private OtpGenerationPort otpGenerator;

    private OtpService otpService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        otpService = new OtpService(otpGenerator, hasher, config, otpRepository);

        when(otpGenerator.generate(eq(OTP_SIZE))).thenReturn(OTP_VALUE);
    }

    @Test
    void createVerificationOtp_shouldReturnAndPersistVerificationData() {

        var hashedOtp = hasher.hashSecurely(OTP_VALUE);
        var res = otpService.createVerificationOtp(EMAIL_ADDRESS);
        var persistedOtp = otpRepository.load(EMAIL_ADDRESS.getValue());

        assertEquals(OTP_VALUE, res.rawOtpCode());
        assertTrue(persistedOtp.isPresent());
        assertEquals(hashedOtp, persistedOtp.get().getValue());
        assertEquals(EMAIL_ADDRESS.getValue(), persistedOtp.get().getKey());
    }

    @Test
    void verifyAndConsumeOtp_should_delete_otp_when_verification_completed() {

        var hashedOtp = hashAndPersistDefaultOtp();

        var persistedOtp = otpRepository.load(EMAIL_VALUE);
        assertTrue(persistedOtp.isPresent());
        assertEquals(hashedOtp, persistedOtp.get().getValue());

        otpService.verifyAndConsumeOtp(EMAIL_ADDRESS,OTP_VALUE);

        var otpAfterVerification = otpRepository.load(EMAIL_VALUE);
        assertTrue(otpAfterVerification.isEmpty());
    }

    @Test
    void should_throw_exception_when_otp_is_expired() {
        // Given
        hashAndPersistDefaultOtp();

        Clock fixedClock = getClockAfterExpirationTime();
        var testConfig = new StubOtpConfig(OTP_SIZE,TTL_IN_SECONDS,fixedClock);
        var testOtpService = new OtpService(otpGenerator, hasher, testConfig, otpRepository);

        // When / Then
        var exception = assertThrows(OtpVerificationException.class, () -> testOtpService.verifyAndConsumeOtp(EMAIL_ADDRESS,OTP_VALUE));
        assertThat(exception).hasMessageContaining("has expired");
    }

    private Clock getClockAfterExpirationTime() {
        return Clock.fixed(Instant.now().plusSeconds(TTL_IN_SECONDS + 1), ZoneId.of("Z"));
    }

    @Test
    void should_throw_exception_when_otp_value_does_not_match() {
        // Given
        hashAndPersistDefaultOtp();
        var persistedOtp = otpRepository.load(EMAIL_VALUE);
        assertTrue(persistedOtp.isPresent());

        var invalidOtpValue = "654321";

        // When / Then
        var exception = assertThrows(
                OtpVerificationException.class,
                () -> otpService.verifyAndConsumeOtp(EMAIL_ADDRESS, invalidOtpValue));

        assertThat(exception).hasMessageContaining("Otp does not match");
    }

    @Test
    void should_throw_exception_when_otp_for_given_email_does_not_exist() {
        // Given
        var invalidEmail = new EmailAddress("invalid@bankapp.online");

        // When / Then
        var exception = assertThrows(OtpVerificationException.class, () -> otpService.verifyAndConsumeOtp(invalidEmail, OTP_VALUE));
        assertThat(exception).hasMessageContaining("No such OTP in the system");
    }


    private String hashAndPersistDefaultOtp() {
        var hashedOtp = hasher.hashSecurely(OTP_VALUE);
        var otp = Otp.createNew(EMAIL_VALUE, hashedOtp, CLOCK, TTL);
        otpRepository.save(otp);
        return hashedOtp;
    }
}