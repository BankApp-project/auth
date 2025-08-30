package bankapp.auth.infrastructure.persistance.otp.config;

import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.persistance.otp.OtpMixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class OtpRedisConfiguration {


    /// Creates and configures a RedisTemplate for OTP operations.
    /// <p>
    /// This template is specifically configured to handle OTP (One-Time Password) objects
    /// with String keys. It uses Jackson JSON serialization for values and String serialization
    /// for keys, with custom OTP mixing applied for proper JSON handling.
    /// </p>
    ///
    /// @param connectionFactory the Redis connection factory for establishing connections
    /// @param objectMapper the Jackson ObjectMapper for JSON serialization configuration
    /// @return a configured RedisTemplate for String keys and OTP values
    @Bean
    public RedisTemplate<String, Otp> otpRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Otp> template = new RedisTemplate<>();
        objectMapper.addMixIn(Otp.class, OtpMixin.class);
        var valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Otp.class);

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);

        return template;
    }
}
