package bankapp.auth.services;

import bankapp.auth.config.SecurityConfiguration;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = { SecurityConfiguration.class })
@SpringBootTest(classes = { SecureOtpNumberGenerator.class })
class SecureOtpNumberGeneratorTest {

    private static final int OTP_LEN = 6;

    @Autowired
    private SecureOtpNumberGenerator otpGenerator;

    @RepeatedTest(100)
    void generate_should_generate_string_with_given_length() {
        var res = otpGenerator.generate(OTP_LEN);

        assertEquals(OTP_LEN, res.length());
    }

    @Test
    void generate_should_throw_exception_for_zero_length() {
        Exception exception = assertThrows(OtpGenerationException.class, () -> otpGenerator.generate(0));

        assertEquals("PIN length must be greater than 0", exception.getMessage());
        assertInstanceOf(OtpGenerationException.class,exception);
    }

    @Test
    void generate_should_throw_exception_for_length_exceeding_9() {
        Exception exception = assertThrows(OtpGenerationException.class, () -> otpGenerator.generate(10));

        assertEquals("PIN length cannot exceed 9 digits", exception.getMessage());
        assertInstanceOf(OtpGenerationException.class,exception);
    }

    @RepeatedTest(100)
    void generate_should_generate_unique_otps_each_time() {
        var otp1 = otpGenerator.generate(OTP_LEN);
        var otp2 = otpGenerator.generate(OTP_LEN);

        assertNotEquals(otp1, otp2);
    }
}