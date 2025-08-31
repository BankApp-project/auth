package bankapp.auth.infrastructure.usecases.it;


import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.infrastructure.WithRabbitMQContainer;
import bankapp.auth.infrastructure.WithRedisContainer;
import bankapp.auth.infrastructure.persistance.otp.config.OtpConfiguration;
import bankapp.auth.infrastructure.rest.verification.dto.InitiateVerificationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    void should_save_otp_to_redis_when_valid_email_provided() throws Exception {
        var request = new InitiateVerificationRequest(DEFAULT_EMAIL);
        var jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(VERIFICATION_INITIATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            Optional<Otp> res = otpRepository.load(DEFAULT_EMAIL);
            assertThat(res).isPresent();

            Clock clock = otpConfiguration.getClock();
            //noinspection OptionalGetWithoutIsPresent
            assertTrue(res.get().isValid(clock));
        });

    }
}
