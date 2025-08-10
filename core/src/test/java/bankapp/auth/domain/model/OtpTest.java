package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpTest {

    /**
     * Unit tests for the toString method in the Otp class.
     * The toString method is designed to display a masked representation
     * of the OTP value and a shortened version of the key.
     */



    @Test
    void should_generate_unique_id_for_each_otp_instance() {
        //given
        String value = "123456";
        String key = "test@example.com";

        //when
        Otp otp1 = new Otp(value, key);
        Otp otp2 = new Otp(value, key);

        //then
        assertNotEquals(otp1.getId(), otp2.getId(), "Each OTP should have a unique ID");
    }

    @Test
    void should_not_expose_sensitive_data_in_toString() {
        //given
        String sensitiveValue = "123456";
        String key = "test@example.com";
        Otp otp = new Otp(sensitiveValue, key);

        //when
        String otpString = otp.toString();

        //then
        assertFalse(otpString.contains(sensitiveValue),
                "toString should not expose the actual OTP value");
        assertTrue(otpString.contains("******"),
                "toString should mask the OTP value");
    }

    @Test
    void should_properly_mask_key_in_toString() {
        //given
        String value = "123456";
        String longKey = "verylongemail@example.com";
        Otp otp = new Otp(value, longKey);

        //when
        String result = otp.toString();

        //then
        assertTrue(result.contains("ver..."), "Should show only first 3 characters of key");
        assertFalse(result.contains(longKey), "Should not expose full key");
    }

    @Test
    void should_throw_exception_when_value_is_null() {
        //given
        String key = "test@example.com";

        //when & then
        assertThrows(NullPointerException.class, () -> new Otp(null, key),
                "Should throw OtpFormatException when value is null");
    }

    @Test
    void should_throw_exception_when_key_is_null() {
        //given
        String value = "123456";

        //when & then
        assertThrows(NullPointerException.class, () -> new Otp(value, null),
                "Should throw OtpFormatException when key is null");
    }

    @Test
    void should_throw_exception_when_value_is_empty() {
        //given
        String key = "test@example.com";

        //when & then
        assertThrows(OtpFormatException.class, () -> new Otp("", key),
                "Should throw OtpFormatException when value is empty");
    }

    @Test
    void should_throw_exception_when_key_is_empty() {
        //given
        String value = "123456";

        //when & then
        assertThrows(OtpFormatException.class, () -> new Otp(value, ""),
                "Should throw OtpFormatException when key is empty");
    }

}