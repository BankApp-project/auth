package bankapp.auth;

import bankapp.auth.domain.model.Otp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OtpGeneratorTest {

    private final static int OTP_LEN = 6;
    private final OtpGenerator otpGenerator = new OtpGenerator();
    //test generating new Otp()
    @Test
    void should_generate_and_return_new_Otp_with_given_length() {
        Otp otp = generateDefaultOtp();
        Assertions.assertEquals(OTP_LEN, otp.getValue().length());
    }

    @Test
    void should_generate_and_return_distinct_Otp() {
        Otp otp = generateDefaultOtp();
        Otp nextOtp = generateDefaultOtp();

        assertNotEquals(otp, nextOtp);
        Assertions.assertNotEquals(otp.getValue(), nextOtp.getValue());
    }

    @Test
    void should_generate_and_return_otp_with_same_email() {
        Otp otp = generateDefaultOtp();
        Otp nextOtp = generateDefaultOtp();

        Assertions.assertEquals(otp.getKey(), nextOtp.getKey());
    }

    @Test
    void should_generate_digit_only_otp() {
        Assertions.assertTrue(generateDefaultOtp().getValue().matches("\\d+"));
    }

    private Otp generateDefaultOtp() {
        String validEmail = "valid@bankapp.online";
        return otpGenerator.generate(validEmail, OTP_LEN);
    }
}
