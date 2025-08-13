package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.service.StubOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerifyEmailOtpUseCasePersistenceTest {

   /*
    Test Case 2: Verify Otp

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: User is prompted with passkey creation form.

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: handler should return true. so FE can send passkey creation form.
    */

    private static final int DEFAULT_TTL = 98;
    private static final String INVALID_OTP_KEY = "nonexisting@bankapp.online";
    private final static String VALID_OTP_KEY = "test@bankapp.online";
    private final static String VALID_OTP_VALUE = "123456";

    private final OtpRepository otpRepository = new StubOtpRepository();

    private VerifyEmailOtpCommand command;
    private VerifyEmailOtpUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock standardClock = Clock.systemUTC();
        Otp VALID_OTP = new Otp(VALID_OTP_VALUE, VALID_OTP_KEY);
        VALID_OTP.setExpirationTime(standardClock, DEFAULT_TTL);
        otpRepository.save(VALID_OTP);
        command = new VerifyEmailOtpCommand(VALID_OTP);
        useCase = new VerifyEmailOtpUseCase(standardClock, otpRepository);
    }

        @Test
    void should_load_otp_when_otp_with_valid_email_provided() {

        useCase.handle(command);

        assertThat(otpRepository.load(VALID_OTP_KEY)).isNotNull();
    }

    @Test
    void should_throw_exception_when_provide_non_existing_email() {
        Otp otpWithInvalidkey = new Otp(VALID_OTP_VALUE, INVALID_OTP_KEY);
        VerifyEmailOtpCommand invalidCommand = new VerifyEmailOtpCommand(otpWithInvalidkey);


        var exception = assertThrows(VerifyEmailOtpException.class,() -> useCase.handle(invalidCommand));
        assertThat(exception).hasMessageContaining("No such OTP in the system");
    }
}