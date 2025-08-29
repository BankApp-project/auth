package bankapp.auth.services;

import bankapp.auth.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = { SecurityConfiguration.class })
@SpringBootTest(classes = { BCryptHashingService.class })
class BCryptHashingServiceTest {

    String DEFAULT_VALUE = "123456";

    @Autowired
    private BCryptHashingService hashingService;

    @Test
    void hashSecurely_should_return_different_nonNull_value() {
        var value = hashingService.hashSecurely(DEFAULT_VALUE);

        assertFalse(value.isBlank());
        assertNotEquals(DEFAULT_VALUE, value);
    }

    @Test
    void hashSecurely_should_return_value_hashed_by_bcrypt() {
        var hasher = new BCryptPasswordEncoder();

        var value = hashingService.hashSecurely(DEFAULT_VALUE);

        var valueMatches = hasher.matches(DEFAULT_VALUE, value);

        assertTrue(valueMatches);
    }

    @Test
    void hashSecurely_should_throw_exception_when_null_input() {

        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> hashingService.hashSecurely(null));
    }

    @Test
    void verify_should_return_false_if_values_doesnt_match() {

        var hasher = new BCryptPasswordEncoder();
        var hashedValue = hasher.encode(DEFAULT_VALUE);

        assertFalse(hashingService.verify(hashedValue,"123123"));
    }

    @Test
    void verify_should_return_true_if_Values_does_match() {

        var hasher = new BCryptPasswordEncoder();
        var hashedValue = hasher.encode(DEFAULT_VALUE);

        var result = hashingService.verify(hashedValue,DEFAULT_VALUE);
        assertTrue(result);
    }
}