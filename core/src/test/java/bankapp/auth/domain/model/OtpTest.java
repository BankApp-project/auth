package bankapp.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OtpTest {

    /**
     * Unit tests for the toString method in the Otp class.
     * The toString method is designed to display a masked representation
     * of the OTP value and a shortened version of the key.
     */


    @Test
    void testToString_WhenKeyAndValueArePresent() {
        // Arrange
        String key = "abc123";
        String value = "123456";
        Otp otp = new Otp(value, key);

        // Act
        String result = otp.toString();

        // Assert
        assertEquals("OTP[value=******, key=abc...]", result);
    }

    @Test
    void testToString_WhenKeyIsShorterThanThreeCharacters() {
        // Arrange
        String key = "ab";
        String value = "654321";
        Otp otp = new Otp(value, key);

        // Act
        String result = otp.toString();

        // Assert
        assertEquals("OTP[value=******, key=ab...]", result);
    }

    @Test
    void testToString_WhenKeyIsNull() {
        // Arrange
        String key = null;
        String value = "987654";
        Otp otp = new Otp(value, key);

        // Act
        String result = otp.toString();

        // Assert
        assertEquals("OTP[value=******, key=null]", result);
    }

    @Test
    void testToString_WhenKeyIsEmpty() {
        // Arrange
        String key = "";
        String value = "456789";
        Otp otp = new Otp(value, key);

        // Act
        String result = otp.toString();

        // Assert
        assertEquals("OTP[value=******, key=...]", result);
    }
}