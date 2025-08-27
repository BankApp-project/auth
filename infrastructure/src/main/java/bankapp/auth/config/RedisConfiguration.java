package bankapp.auth.config;

import bankapp.auth.domain.model.Otp;
import bankapp.auth.persistance.otp.OtpMixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

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
