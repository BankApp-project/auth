package bankapp.auth.domain;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.stubs.StubHasher;
import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;
import bankapp.auth.domain.port.out.stubs.OtpConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class OtpServiceTest {

    private static final int OTP_SIZE = 6;
    private static final int TTL_IN_SECONDS = 60;
    private static final Clock CLOCK = Clock.systemUTC();
    private static final String OTP_VALUE = "123456";
    private static final EmailAddress EMAIL_ADDRESS = new EmailAddress("test@bankapp.online");

    private final HashingPort hasher = new StubHasher();
    private final OtpConfigPort config = new OtpConfig(OTP_SIZE, TTL_IN_SECONDS, CLOCK);

    @Mock
    private OtpGenerationPort otpGenerator;

    private OtpService otpService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        otpService = new OtpService(otpGenerator, hasher, config);

        when(otpGenerator.generate(eq(OTP_SIZE))).thenReturn(OTP_VALUE);
    }

    @Test
    void createVerificationOtp_shouldReturnVerificationData() {

        var hashedOtp = hasher.hashSecurely(OTP_VALUE);
        var res = otpService.createVerificationOtp(EMAIL_ADDRESS);

        assertEquals(OTP_VALUE, res.rawOtpCode());
        assertEquals(hashedOtp, res.otpToPersist().getValue());
        assertEquals(EMAIL_ADDRESS.getValue(), res.otpToPersist().getKey());
    }
}