package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.service.StubOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerifyEmailOtpUseCaseValidationTest {


    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    private static final int DEFAULT_TTL = 98;
    private final static String VALID_OTP_KEY = "test@bankapp.online";
    private final static String VALID_OTP_VALUE = "123456";

    private final OtpRepository otpRepository = new StubOtpRepository();

    private VerifyEmailOtpCommand command;
    private VerifyEmailOtpUseCase useCase;

    @BeforeEach
    void setUp() {
        Otp VALID_OTP = new Otp(VALID_OTP_VALUE, VALID_OTP_KEY);
        VALID_OTP.setExpirationTime(DEFAULT_CLOCK, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        command = new VerifyEmailOtpCommand(VALID_OTP);
    }

    @Test
    void should_throw_exception_when_otp_value_doesnt_match_with_valid_key() {
        useCase = new VerifyEmailOtpUseCase(DEFAULT_CLOCK, otpRepository);

        Otp otpWithInvalidValue = new Otp("9999", VALID_OTP_KEY);
        VerifyEmailOtpCommand testCommand = new VerifyEmailOtpCommand(otpWithInvalidValue);

        var exception = assertThrows(VerifyEmailOtpException.class, () -> useCase.handle(testCommand));
        assertThat(exception).hasMessageContaining("does not match");
    }

    @Test
    void should_throw_exception_when_otp_match_but_expired() {
        Clock clock = Clock.fixed(Instant.now().plusSeconds(DEFAULT_TTL + 1), ZoneId.of("Z"));
        useCase = new VerifyEmailOtpUseCase(clock, otpRepository);

        var exception = assertThrows(VerifyEmailOtpException.class, () -> useCase.handle(command));
        assertThat(exception).hasMessageContaining("has expired");
    }
}
