package bankapp.auth.application.verify_otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.verify_otp.in.commands.VerifyEmailOtpCommand;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.service.StubOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VerifyEmailOtpUseCaseTest {

   /*
    Test Case 2: Verify Otp

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: User is prompted with passkey creation form.

    Given: new user is on the login page
    When: new user provided valid otp to `provide otp` form and clicks `continue` button / presses enter
    Then: handler should return true. so FE can send passkey creation form.
    */


    private final static String VALID_OTP_KEY = "test@bankapp.online";
    public static final String INVALID_OTP_KEY = "nonexisting@bankapp.online";
    public static final String VALID_OTP_VALUE = "123456";
    public static final Otp VALID_OTP = new Otp(VALID_OTP_VALUE, VALID_OTP_KEY);

    private final OtpRepository otpRepository = new StubOtpRepository();
    private final VerifyEmailOtpCommand command = new VerifyEmailOtpCommand(VALID_OTP);
    private final VerifyEmailOtpUseCase useCase = new VerifyEmailOtpUseCase(otpRepository);

    @BeforeEach
    void setUp() {
        otpRepository.save(VALID_OTP,10);
    }

    @Test
    void should_load_otp_when_otp_with_valid_email_provided() {

        useCase.handle(command);

        assertThat(otpRepository.load(VALID_OTP_KEY)).isNotNull();
    }

    @Test
    void should_return_true_when_provide_existing_email() {

        var good = useCase.handle(command);

        assertThat(good).isTrue();
    }

    @Test
    void should_return_false_when_provide_non_existing_email() {
        var otpWithInvalidkey = new Otp(VALID_OTP_VALUE, INVALID_OTP_KEY);
        var invalidCommand = new VerifyEmailOtpCommand(otpWithInvalidkey);

        var good = useCase.handle(invalidCommand);

        assertThat(good).isFalse();
    }

}
// should return false when otp expired