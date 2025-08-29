package bankapp.auth.services;

import bankapp.auth.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = { SecurityConfiguration.class })
@SpringBootTest(classes = { BCryptHashingService.class })
class BCryptHashingServiceTest {

    String DEFAULT_VALUE = "123456";

    @Autowired
    private PasswordEncoder passwordEncoder;

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

}