package bankapp.auth.infrastructure.driven.session;

import com.github.f4b6a3.uuid.util.UuidUtil;
import com.github.f4b6a3.uuid.util.UuidValidator;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionIdGeneratorTest {

    private final static Clock CLOCK = Clock.systemUTC();
    private final static SecureRandom SECURE_RANDOM = new SecureRandom();

    @Test
    void generate_should_generate_valid_UUID_V7() {
        var generator = new SessionIdGenerator(CLOCK, SECURE_RANDOM);

        var res = generator.generate();

        //verify if res is valid uuid
        assertTrue(UuidValidator.isValid(res));
        //verify if res is uuid v7
        assertTrue(UuidUtil.isTimeOrderedEpoch(res));
    }
}