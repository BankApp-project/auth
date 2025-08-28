package bankapp.auth.persistance.otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.port.out.OtpConfigPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisOtpRepository implements OtpRepository {

    private final RedisTemplate<String, Otp> redisTemplate;
    private final OtpConfigPort otpConfig;

    @Override
    public void save(@NonNull Otp otp) {
        String key = otp.getKey();
        Duration timeout = otpConfig.getTtl();

        redisTemplate.opsForValue().set(key, otp, timeout);
    }

    @Override
    public Optional<Otp> load(@NonNull String key) {
        Otp otp = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(otp);
    }

    @Override
    public void delete(@NonNull String key) {
        redisTemplate.delete(key);
    }
}
