package bankapp.auth.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class AmqpConfiguration {

    @Bean
    public TopicExchange notificationTopicExchange(@Value("${app.amqp.publisher.notifications.otp.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public MessageConverter amqpMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
