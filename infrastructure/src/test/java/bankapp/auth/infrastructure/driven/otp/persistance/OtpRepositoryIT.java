package bankapp.auth.infrastructure.driven.otp.persistance;

import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.WithRedisContainer;
import bankapp.auth.infrastructure.crosscutting.config.ClockConfiguration;
import bankapp.auth.infrastructure.crosscutting.config.JSONConfiguration;
import bankapp.auth.infrastructure.driven.otp.config.OtpConfiguration;
import bankapp.auth.infrastructure.driven.otp.config.OtpRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataRedisTest
@ActiveProfiles("test")
@Import({
        RedisOtpRepository.class,
        OtpRedisConfiguration.class,
        OtpConfiguration.class,
        ClockConfiguration.class,
        JSONConfiguration.class
})
public class OtpRepositoryIT implements WithRedisContainer {


    public static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
    public static final Duration TTL = Duration.ofSeconds(300);
    public static final String DEFAULT_KEY = "test-key";
    public static final String DEFAULT_VALUE = "123123";

    @Autowired
    private RedisOtpRepository redisOtpRepository;

    @Test
    void should_save_and_load_otp_with_correct_ttl() {

        Otp testOtp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, CLOCK, TTL);

        redisOtpRepository.save(testOtp);

        var resultOtpOptional = redisOtpRepository.load(DEFAULT_KEY);
        assertThat(resultOtpOptional).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        var resultOtp = resultOtpOptional.get();
        assertThat(resultOtp)
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

        Otp testOtp = Otp.createNew(DEFAULT_KEY, DEFAULT_VALUE, CLOCK, TTL);

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
