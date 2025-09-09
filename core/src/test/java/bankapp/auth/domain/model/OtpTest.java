package bankapp.auth.domain.model;

import bankapp.auth.domain.model.exception.OtpFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class OtpTest {

    private static final String DEFAULT_VALUE = "123456";
    private static final String DEFAULT_KEY = "test@bankapp.online";
    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    private static final Duration TTL = Duration.ofSeconds(60);
    private static final Instant DEFAULT_EXPIRATION_TIME = Instant.now(DEFAULT_CLOCK).plus(TTL);
    private static Otp DEFAULT_OTP;


    /**
     * Unit tests for the toString method in the Otp class.
     * The toString method is designed to display a masked representation
     * of the OTP value and a shortened version of the key.
     */


    @BeforeEach
    void setup() {
        DEFAULT_OTP = Otp.createNew(DEFAULT_VALUE, DEFAULT_KEY, DEFAULT_CLOCK, TTL);
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
        assertThrows(NullPointerException.class, () -> Otp.createNew(null, DEFAULT_KEY, DEFAULT_CLOCK, TTL),
                "Should throw OtpFormatException when value is null");
    }

    @Test
    void should_throw_exception_when_key_is_null() {
        //when & then
        assertThrows(NullPointerException.class, () -> Otp.createNew(DEFAULT_VALUE, null, DEFAULT_CLOCK, TTL),
                "Should throw OtpFormatException when key is null");
    }

    @Test
    void should_throw_exception_when_value_is_empty() {

        //when & then
        assertThrows(OtpFormatException.class, () -> Otp.createNew("", DEFAULT_KEY, DEFAULT_CLOCK, TTL),
                "Should throw OtpFormatException when value is empty");
    }

    @Test
    void should_throw_exception_when_key_is_empty() {
        //given

        //when & then
        assertThrows(OtpFormatException.class, () -> Otp.createNew(DEFAULT_VALUE, "", DEFAULT_CLOCK, TTL),
                "Should throw OtpFormatException when key is empty");
    }

    @Test
    void should_be_equals_when_same_value_and_key() {

        Otp otp2 = Otp.createNew(DEFAULT_VALUE, DEFAULT_KEY, DEFAULT_CLOCK, TTL);

        assertEquals(DEFAULT_OTP, otp2);
    }

    @Test
    void should_not_be_equals_when_different_key_but_same_value() {

        Otp otp2 = Otp.createNew(DEFAULT_VALUE, "differentKey", DEFAULT_CLOCK, TTL);

        assertNotEquals(DEFAULT_OTP, otp2);
    }

    @Test
    void should_not_be_equals_when_same_key_but_different_value() {
        Otp otp2 = Otp.createNew("9999", DEFAULT_KEY, DEFAULT_CLOCK, TTL);

        assertNotEquals(DEFAULT_OTP, otp2);
    }

    @Test
    void should_be_able_to_set_expiration_time_in_minutes() {
        Otp otp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, DEFAULT_CLOCK, TTL);

        assertNotNull(otp.getExpirationTime());
    }

    @Test
    void should_not_be_expired_if_clock_is_just_before_expiration_time() {
        Otp otp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, DEFAULT_CLOCK, TTL);
        Clock justBeforeExpirationClock = Clock.fixed(DEFAULT_EXPIRATION_TIME.minusSeconds(1), ZoneId.of("Z"));
        assertTrue(otp.isValid(justBeforeExpirationClock));
    }

    @Test
    void should_be_expired_if_clock_is_just_after_expiration_time() {
        Otp otp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, DEFAULT_CLOCK, TTL);
        Clock justAfterExpirationClock = Clock.fixed(DEFAULT_EXPIRATION_TIME.plusSeconds(1), ZoneId.of("Z"));
        assertFalse(otp.isValid(justAfterExpirationClock));
    }
}