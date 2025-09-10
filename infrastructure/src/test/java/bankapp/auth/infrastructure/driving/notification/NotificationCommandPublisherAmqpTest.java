package bankapp.auth.infrastructure.driving.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCommandPublisherAmqpTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SendEmailNotificationCommand sendEmailNotificationCommand;

    private NotificationCommandPublisherAmqp publisher;

    private static final String TEST_ROUTING_KEY = "test.routing.key";
    private static final String TEST_EXCHANGE = "test.exchange";

    @BeforeEach
    void setUp() {
        publisher = new NotificationCommandPublisherAmqp(rabbitTemplate);

        // Set the @Value annotated fields using ReflectionTestUtils
        ReflectionTestUtils.setField(publisher, "routingKey", TEST_ROUTING_KEY);
        ReflectionTestUtils.setField(publisher, "exchangeName", TEST_EXCHANGE);
    }

    @Test
    void publishSendEmailCommand_ShouldCallRabbitTemplateWithCorrectParameters() {
        // When
        publisher.publishSendEmailCommand(sendEmailNotificationCommand);

        // Then
        verify(rabbitTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, sendEmailNotificationCommand);
    }

    @Test
    void should_throw_exception_if_command_is_null() {
        // When
        var exception = assertThrows(NotificationCommandPublisherException.class, () -> publisher.publishSendEmailCommand(null));

        assertThat(exception.getMessage())
                .containsIgnoringCase("command")
                .containsIgnoringCase("null");
    }
}
