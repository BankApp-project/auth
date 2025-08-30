package bankapp.auth.infrastructure.persistance.otp;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.port.out.OtpConfigPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;


/// A Redis-based implementation of the {@link OtpRepository} interface.
/// This class provides methods to store, retrieve, and delete One-Time Password (OTP) entities
/// in a Redis data store, utilizing Redis key-value storage and time-to-live (TTL) housekeeping.
///
/// The repository performs the following operations:
///
/// - Saves OTP objects with a specified TTL based on the configuration.
/// - Retrieves OTP objects by their key.
/// - Deletes OTP entities by their key.
///
/// This implementation ensures that OTPs are efficiently persisted and automatically purged
/// by leveraging Redis's TTL mechanism to enforce expiration.
///
/// Note:
/// The TTL feature is used for resource management and does not serve as the primary validation
/// mechanism for OTP expiration; that responsibility falls on the use case layer.
///
/// Dependencies:
/// - {@link RedisTemplate}: Manages Redis operations.
/// - {@link OtpConfigPort}: Provides OTP configuration details such as TTL duration.
@Repository
@RequiredArgsConstructor
public class RedisOtpRepository implements OtpRepository {

    private final RedisTemplate<String, Otp> redisTemplate;
    private final OtpConfigPort otpConfig;

    /// Persists an OTP entity to Redis with automatic expiration.
    ///
    /// This method stores the provided OTP object in Redis using the OTP's key as the Redis key.
    /// The OTP will automatically expire after the duration specified by the OTP configuration's TTL,
    /// leveraging Redis's native expiration mechanism for efficient cleanup.
    ///
    /// @param otp the OTP entity to save; must not be null
    /// @throws IllegalArgumentException if the OTP or its key is null
    @Override
    public void save(@NonNull Otp otp) {
        String key = otp.getKey();
        Duration timeout = otpConfig.getTtl();
        redisTemplate.opsForValue().set(key, otp, timeout);
    }

    /// Retrieves an OTP entity from Redis by its key.
    ///
    /// This method attempts to load an OTP object from Redis using the provided key.
    /// If the key exists and the OTP hasn't expired, it returns the OTP wrapped in an Optional.
    /// If the key doesn't exist or the OTP has expired (due to Redis TTL), an empty Optional is returned.
    ///
    /// @param key the unique identifier for the OTP; must not be null
    /// @return an Optional containing the OTP if found and not expired, or empty otherwise
    /// @throws IllegalArgumentException if the key is null
    @Override
    public Optional<Otp> load(@NonNull String key) {
        Otp otp = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(otp);
    }

    /// Removes an OTP entity from Redis by its key.
    ///
    /// This method immediately deletes the OTP associated with the provided key from Redis.
    /// If the key doesn't exist, the operation completes without error.
    /// This method is typically used for explicit cleanup when an OTP is consumed or invalidated,
    /// rather than waiting for natural TTL expiration.
    ///
    /// @param key the unique identifier for the OTP to delete; must not be null
    /// @throws IllegalArgumentException if the key is null
    @Override
    public void delete(@NonNull String key) {
        redisTemplate.delete(key);
    }

}
