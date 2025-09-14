package bankapp.auth.infrastructure.driven.challenge.config;

import bankapp.auth.application.shared.port.out.dto.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ChallengeRedisConfig {

    @Bean
    public RedisTemplate<String, Session> challengeRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Session> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Session> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Session.class);
        template.setValueSerializer(serializer);

        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}
