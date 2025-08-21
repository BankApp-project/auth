package bankapp.auth.domain;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.verification_initiate.VerificationData;
import bankapp.auth.application.verification_initiate.port.out.OtpGenerationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.port.out.OtpConfigPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OtpServiceTest {

    @Mock
    private OtpGenerationPort otpGenerator;

    @Mock
    private HashingPort hasher;

    @Mock
    private Clock clock;

    @Mock
    private OtpConfigPort config;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create verification OTP successfully")
    void createVerificationOtp_shouldReturnVerificationData() {
        // Given
        EmailAddress email = new EmailAddress("test@example.com");
        String rawOtp = "123456";
        String hashedOtp = "hashed123456";
        int otpSize = 6;
        int ttlInMinutes = 10;
        Instant now = Instant.now();
        Instant expirationTime = now.plus(ttlInMinutes, ChronoUnit.MINUTES);

        when(config.getOtpSize()).thenReturn(otpSize);
        when(config.getTtlInMinutes()).thenReturn(ttlInMinutes);
        when(otpGenerator.generate(otpSize)).thenReturn(rawOtp);
        when(hasher.hashSecurely(rawOtp)).thenReturn(hashedOtp);
        when(clock.instant()).thenReturn(now);

        // When
        VerificationData verificationData = otpService.createVerificationOtp(email);

        // Then
        assertEquals(rawOtp, verificationData.rawOtpCode());
        assertEquals(email.getValue(), verificationData.otpToPersist().getKey());
        assertEquals(hashedOtp, verificationData.otpToPersist().getValue());
        assertEquals(expirationTime, verificationData.otpToPersist().getExpirationTime());
    }
}