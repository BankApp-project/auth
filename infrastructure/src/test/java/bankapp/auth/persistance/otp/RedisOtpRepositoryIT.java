package bankapp.auth.persistance.otp;

import bankapp.auth.RedisIntegrationTestBase;
import bankapp.auth.domain.model.Otp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RedisOtpRepositoryIT extends RedisIntegrationTestBase {


    public static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
    public static final long TTL_IN_SECONDS = 300L;
    public static final String DEFAULT_KEY = "test-key";
    public static final String DEFAULT_VALUE = "123123";
    @Autowired
    private RedisOtpRepository redisOtpRepository;

    @Test
    void should_save_and_load_otp_with_correct_ttl() {

        Otp testOtp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, CLOCK, TTL_IN_SECONDS);

        redisOtpRepository.save(testOtp);

        var resultOtpOptional = redisOtpRepository.load(DEFAULT_KEY);
        assertThat(resultOtpOptional).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        var resultOtp = resultOtpOptional.get();
        assertThat(resultOtp)
                .usingRecursiveComparison()
                .isEqualTo(testOtp);
    }

    @Test
    void should_return_empty_optional_when_otp_doesnt_exists() {
        String nonExistingKey = "nonExistingKey";
        var resOtp = redisOtpRepository.load(nonExistingKey);

        assertThat(resOtp).isEmpty();
    }
    
    @Test
    void should_delete_entry_when_valid_key_provided() {

        Otp testOtp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, CLOCK, TTL_IN_SECONDS);

        redisOtpRepository.save(testOtp);

        redisOtpRepository.delete(DEFAULT_KEY);

        var res = redisOtpRepository.load(DEFAULT_KEY);

        assertThat(res).isEmpty();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void should_throw_exception_when_null_values_passed_to_any_method() {

        assertThrows(NullPointerException.class, () -> redisOtpRepository.save(null));
        assertThrows(NullPointerException.class, () -> redisOtpRepository.load(null));
        assertThrows(NullPointerException.class, () -> redisOtpRepository.delete(null));
    }
}
