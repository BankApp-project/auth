package bankapp.auth.infrastructure.usecases.it;


import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.AmqpOtpTestConfig;
import bankapp.auth.infrastructure.WithRabbitMQContainer;
import bankapp.auth.infrastructure.WithRedisContainer;
import bankapp.auth.infrastructure.persistance.otp.config.OtpConfiguration;
import bankapp.auth.infrastructure.rest.verification.dto.InitiateVerificationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Import(AmqpOtpTestConfig.class)
public class InitiateVerificationIT implements WithRedisContainer, WithRabbitMQContainer {

    public static final String VERIFICATION_INITIATE_ENDPOINT = "/verification/initiate/email";

    private final String DEFAULT_EMAIL = "test@bankapp.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private OtpConfiguration otpConfiguration;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Queue testQueue;

    @Test
    void should_save_otp_to_redis_when_valid_email_provided() throws Exception {
        //given
        var request = new InitiateVerificationRequest(DEFAULT_EMAIL);
        var jsonRequest = objectMapper.writeValueAsString(request);

        //when
        mockMvc.perform(post(VERIFICATION_INITIATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isAccepted());

        //then
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            Optional<Otp> res = otpRepository.load(DEFAULT_EMAIL);
            assertThat(res).isPresent();

            Clock clock = otpConfiguration.getClock();
            //noinspection OptionalGetWithoutIsPresent
            assertTrue(res.get().isValid(clock));
        });
    }

    @Test
    void should_publish_command_to_rabbitMQ_when_valid_email_provided() throws Exception {
        //given
        var request = new InitiateVerificationRequest(DEFAULT_EMAIL);
        var jsonRequest = objectMapper.writeValueAsString(request);

        //when
        mockMvc.perform(post(VERIFICATION_INITIATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isAccepted());

        //then
        var receivedPayload = rabbitTemplate.receiveAndConvert(testQueue.getActualName(), 500L);

        Assertions.assertNotNull(receivedPayload);
        assertThat(receivedPayload.toString()).containsIgnoringCase(DEFAULT_EMAIL);

    }
}
