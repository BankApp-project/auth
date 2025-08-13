package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class OtpTest {

    private static final String DEFAULT_VALUE = "123456";
    private static final String DEFAULT_KEY = "test@bankapp.online";
    private static Otp DEFAULT_OTP;


    /**
     * Unit tests for the toString method in the Otp class.
     * The toString method is designed to display a masked representation
     * of the OTP value and a shortened version of the key.
     */


    @BeforeEach
    void setup() {
       DEFAULT_OTP = new Otp(DEFAULT_VALUE, DEFAULT_KEY);
    }

    @Test
    void should_not_expose_sensitive_data_in_toString() {
        //when
        String otpString = DEFAULT_OTP.toString();

        //then
        assertFalse(otpString.contains(DEFAULT_VALUE),
                "toString should not expose the actual OTP value");
    }

    @Test
    void should_properly_mask_key_in_toString() {
        //when
        String result = DEFAULT_OTP.toString();

        //then
        assertFalse(result.contains(DEFAULT_KEY.substring(3)), "Should show only first 3 characters of key");
        assertFalse(result.contains(DEFAULT_KEY), "Should not expose full key");
    }

    @Test
    void should_throw_exception_when_value_is_null() {
        //when & then
        assertThrows(NullPointerException.class, () -> new Otp(null, DEFAULT_KEY),
                "Should throw OtpFormatException when value is null");
    }

    @Test
    void should_throw_exception_when_key_is_null() {
        //when & then
        assertThrows(NullPointerException.class, () -> new Otp(DEFAULT_VALUE, null),
                "Should throw OtpFormatException when key is null");
    }

    @Test
    void should_throw_exception_when_value_is_empty() {

        //when & then
        assertThrows(OtpFormatException.class, () -> new Otp("", DEFAULT_KEY),
                "Should throw OtpFormatException when value is empty");
    }

    @Test
    void should_throw_exception_when_key_is_empty() {
        //given

        //when & then
        assertThrows(OtpFormatException.class, () -> new Otp(DEFAULT_VALUE, ""),
                "Should throw OtpFormatException when key is empty");
    }

    @Test
    void should_be_equals_when_same_value_and_key() {

        Otp otp2 = new Otp(DEFAULT_VALUE, DEFAULT_KEY);

        assertEquals(DEFAULT_OTP, otp2);
    }

    @Test
    void should_not_be_equals_when_different_key_but_same_value() {

        Otp otp2 = new Otp(DEFAULT_VALUE, "differentKey");

        assertNotEquals(DEFAULT_OTP, otp2);
    }

    @Test
    void should_not_be_equals_when_same_key_but_different_value() {
        Otp otp2 = new Otp("9999", DEFAULT_KEY);

        assertNotEquals(DEFAULT_OTP, otp2);
    }

    @Test
    void should_be_able_to_set_expiration_time_in_minutes() {
        DEFAULT_OTP.setExpirationTime(5);

        assertNotNull(DEFAULT_OTP.getExpirationTime());
    }

    @Test
    void should_return_true_if_not_expired() {
        DEFAULT_OTP.setExpirationTime(5);

        assertTrue(DEFAULT_OTP.isValid());
    }

    @Test
    void should_return_false_if_expired() {
        DEFAULT_OTP.setExpirationTime(5);
        DEFAULT_OTP.setClock(Clock.fixed(Instant.now().plusSeconds(6), ZoneId.of("Z")));
        assertFalse(DEFAULT_OTP.isValid());
    }
}