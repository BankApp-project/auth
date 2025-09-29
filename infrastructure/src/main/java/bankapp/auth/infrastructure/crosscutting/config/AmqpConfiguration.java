package bankapp.auth.infrastructure.crosscutting.config;

import com.fasterxml.jackson.databind.json.JsonMapper;
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
        //todo define specific durable exchange?
        return new TopicExchange(exchangeName);
    }

    @Bean
    public MessageConverter amqpMessageConverter(JsonMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
