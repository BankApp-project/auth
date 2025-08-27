package bankapp.auth.persistance.otp;

import bankapp.auth.RedisIntegrationTestBase;
import bankapp.auth.domain.model.Otp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RedisOtpRepositoryIT extends RedisIntegrationTestBase {


    @Autowired
    private RedisOtpRepository redisOtpRepository;

    @Test
    void should_save_and_load_otp_with_correct_ttl() {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        long ttlInSeconds = 300L;
        String key = "test-key";

        Otp testOtp = Otp.createNew(key, "123123", clock, ttlInSeconds);

        redisOtpRepository.save(testOtp);

        var resultOtpOptional = redisOtpRepository.load(key);
        assertThat(resultOtpOptional).isPresent();
        var resultOtp = resultOtpOptional.get();
        assertThat(resultOtp)
                .usingRecursiveComparison()
                .isEqualTo(testOtp);
    }
}
