package bankapp.auth.domain.model.vo;

import bankapp.auth.domain.model.exception.InvalidEmailFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BDD-style tests for the EmailAddress class.
 * <p>
 * This test class specifically tests the {@link EmailAddress#toString()} method,
 * which returns the email address stored as a string. Each test case uses the BDD style
 * to represent the given-when-then structure.
 */
class EmailAddressTest {

    @Test
    void shouldReturnLowerCaseEmail_whenValidEmailIsGiven() {
        // Given
        String email = "Test.Email@domain-Example.COM";
        EmailAddress emailAddress = new EmailAddress(email);

        // When
        String result = emailAddress.toString();

        // Then
        assertEquals("test.email@domain-example.com", result);
    }

    @Test
    void shouldReturnNormalizedEmail_whenEmailWithUppercaseAndSpecialCharactersIsGiven() {
        // Given
        String email = "USEREmail@Domain-Example.com";
        EmailAddress emailAddress = new EmailAddress(email);

        // When
        String result = emailAddress.toString();

        // Then
        assertEquals("useremail@domain-example.com", result);
    }

    @Test
    void shouldThrowException_whenEmptyEmailIsGiven() {
        // Given
        String email = "";

        // When & Then
        assertThrows(InvalidEmailFormatException.class, () -> new EmailAddress(email));
    }

    @Test
    void shouldThrowException_whenNullEmailIsGiven() {
        // Given
        String email = null;

        // When & Then
        assertThrows(InvalidEmailFormatException.class, () -> new EmailAddress(email));
    }

    @Test
    void shouldThrowException_whenInvalidEmailFormatIsGiven() {
        // Given
        String email = "invalid-email-format";

        // When & Then
        assertThrows(InvalidEmailFormatException.class, () -> new EmailAddress(email));
    }

    @Test
    void shouldThrowException_whenTooLongEmailIsGiven() {
        // Given
        String email = "a".repeat(256) + "@example.com";

        // When & Then
        assertThrows(InvalidEmailFormatException.class, () -> new EmailAddress(email));
    }


    // Tests for allowed special characters in email addresses
    @ParameterizedTest
    @ValueSource(strings = {
            "user.name@example.com",           // dot
            "user_name@example.com",           // underscore
            "user-name@example.com",           // hyphen
            "user+tag@example.com",            // plus
            "user!test@example.com",           // exclamation
            "user#hash@example.com",           // hash
            "user$dollar@example.com",         // dollar
            "user%percent@example.com",        // percent
            "user&ampersand@example.com",      // ampersand
            "user'apostrophe@example.com",     // apostrophe
            "user*asterisk@example.com",       // asterisk
            "user/slash@example.com",          // forward slash
            "user=equals@example.com",         // equals
            "user?question@example.com",       // question mark
            "user^caret@example.com",          // caret
            "user`backtick@example.com",       // backtick
            "user{brace@example.com",          // left brace
            "user|pipe@example.com",           // pipe
            "user}brace@example.com",          // right brace
            "user~tilde@example.com"           // tilde
    })
    void shouldAcceptEmail_whenEmailContainsAllowedSpecialCharacters(String email) {
        // When & Then
        assertDoesNotThrow(() -> new EmailAddress(email));
    }

    @Test
    void shouldAcceptEmail_whenEmailContainsMultipleAllowedSpecialCharacters() {
        // Given
        String email = "user.test+tag_name-123!#$%&'*@example.com";

        // When & Then
        assertDoesNotThrow(() -> new EmailAddress(email));

        // And verify it's properly normalized to lowercase
        EmailAddress emailAddress = new EmailAddress(email);
        assertEquals("user.test+tag_name-123!#$%&'*@example.com", emailAddress.toString());
    }

    @Test
    void shouldAcceptEmail_whenEmailContainsNumbers() {
        // Given
        String email = "user123@example.com";

        // When
        EmailAddress emailAddress = new EmailAddress(email);
        String result = emailAddress.toString();

        // Then
        assertEquals("user123@example.com", result);
    }

    @Test
    void shouldAcceptEmail_whenEmailContainsNumbersAndSpecialCharacters() {
        // Given
        String email = "user123+test_tag@example-domain.com";

        // When
        EmailAddress emailAddress = new EmailAddress(email);
        String result = emailAddress.toString();

        // Then
        assertEquals("user123+test_tag@example-domain.com", result);
    }

    // Tests for characters that might not be supported
    @ParameterizedTest
    @ValueSource(strings = {
            "user,comma@example.com",          // comma (might not be supported without quoting)
            "user@example@domain.com",         // multiple @ symbols
            "user name@example.com",           // space without quotes
            "user..double@example.com",        // consecutive dots
            ".user@example.com",               // leading dot
            "user.@example.com"                // trailing dot
    })
    void shouldRejectEmail_whenEmailContainsUnsupportedCharacters(String email) {
        // When & Then
        assertThrows(InvalidEmailFormatException.class, () -> new EmailAddress(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "@example.com",                    // missing local part
            "user@",                          // missing domain
            "user@@example.com",              // double @
            "user@.example.com",              // domain starts with dot
            "user@example.",                  // domain ends with dot
            "user@example..com"               // consecutive dots in domain
    })
    void shouldRejectEmail_whenEmailHasInvalidFormat(String email) {
        // When & Then
        assertThrows(InvalidEmailFormatException.class, () -> new EmailAddress(email));
    }
}