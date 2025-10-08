package bankapp.auth.infrastructure.usecases.it;


import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.shared.port.out.service.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.service.HashingPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.otp.config.OtpProperties;
import bankapp.auth.infrastructure.driving.rest.verification.complete.dto.CompleteVerificationRequest;
import bankapp.auth.infrastructure.utils.WithPostgresContainer;
import bankapp.auth.infrastructure.utils.WithRedisContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-postgres")
@Transactional // Automatically rolls back database changes after each test
public class CompleteVerificationIT implements WithPostgresContainer, WithRedisContainer {

    private static final String VERIFICATION_COMPLETE_ENDPOINT = "/verification/complete/email";
    private static final String DEFAULT_EMAIL = "test@bankapp.online";
    private static final String DEFAULT_OTP = "123456";
    private static final String INVALID_OTP = "654321";
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
    private static final Duration CHALLENGE_TTL = Duration.ofSeconds(60);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private OtpProperties otpProperties;

    @Autowired
    private HashingPort hasher;

    @MockitoBean
    private ChallengeGenerationPort challengeGeneratorMock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void completeVerification_should_return_valid_registration_response_when_new_user_provide_valid_otp() throws Exception {
        // Arrange
        var hashedOtp = hasher.hashSecurely(DEFAULT_OTP);
        var otp = Otp.createNew(DEFAULT_EMAIL, hashedOtp, clock, otpProperties.ttl());
        otpRepository.save(otp);

        var challenge = createFixedChallenge();
        Mockito.when(challengeGeneratorMock.generate()).thenReturn(challenge);

        var completeVerificationRequest = new CompleteVerificationRequest(DEFAULT_EMAIL, DEFAULT_OTP);

        // Act & Assert
        mockMvc.perform(post(VERIFICATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeVerificationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.registrationOptions.challenge").value(Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.challenge())));

        // Additional Assertions
        // Note: Cannot assert specific challenge is saved because it's generated internally
        assertUserIsCreatedAndDisabled();
    }

    @Test
    void completeVerification_should_save_session_when_new_user_provide_valid_otp() throws Exception {
        // Arrange
        var hashedOtp = hasher.hashSecurely(DEFAULT_OTP);
        var otp = Otp.createNew(DEFAULT_EMAIL, hashedOtp, clock, otpProperties.ttl());
        otpRepository.save(otp);

        var challenge = createFixedChallenge();
        Mockito.when(challengeGeneratorMock.generate()).thenReturn(challenge);

        var completeVerificationRequest = new CompleteVerificationRequest(DEFAULT_EMAIL, DEFAULT_OTP);

        // Act
        var mvcResult = mockMvc.perform(post(VERIFICATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeVerificationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract sessionId from response
        var responseContent = mvcResult.getResponse().getContentAsString();
        var jsonResponse = objectMapper.readTree(responseContent);
        var sessionId = UUID.fromString(jsonResponse.get("sessionId").asText());

        // Assert
        assertSessionIsSaved(sessionId, challenge);
    }

    private void assertUserIsCreatedAndDisabled() {
        var emailObj = new EmailAddress(DEFAULT_EMAIL);
        var userOptional = userRepository.findByEmail(emailObj);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.isEnabled()).isFalse());
    }

    @Test
    void completeVerification_should_return_valid_login_response_when_new_user_provide_valid_otp() throws Exception {
        // Arrange
        var hashedOtp = hasher.hashSecurely(DEFAULT_OTP);
        var otp = Otp.createNew(DEFAULT_EMAIL, hashedOtp, clock, otpProperties.ttl());
        otpRepository.save(otp);

        createAndActivateUser();

        var challenge = createFixedChallenge();
        Mockito.when(challengeGeneratorMock.generate()).thenReturn(challenge);

        var completeVerificationRequest = new CompleteVerificationRequest(DEFAULT_EMAIL, DEFAULT_OTP);

        // Act & Assert
        mockMvc.perform(post(VERIFICATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeVerificationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.loginOptions.challenge").value(Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.challenge())));

        // Additional Assertions
        assertUserIsCreatedAndEnabled();
    }

    @Test
    void completeVerification_should_save_session_when_existing_user_provide_valid_otp() throws Exception {
        // Arrange
        var hashedOtp = hasher.hashSecurely(DEFAULT_OTP);
        var otp = Otp.createNew(DEFAULT_EMAIL, hashedOtp, clock, otpProperties.ttl());
        otpRepository.save(otp);

        createAndActivateUser();

        var challenge = createFixedChallenge();
        Mockito.when(challengeGeneratorMock.generate()).thenReturn(challenge);

        var completeVerificationRequest = new CompleteVerificationRequest(DEFAULT_EMAIL, DEFAULT_OTP);

        // Act
        var mvcResult = mockMvc.perform(post(VERIFICATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeVerificationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract sessionId from response
        var responseContent = mvcResult.getResponse().getContentAsString();
        var jsonResponse = objectMapper.readTree(responseContent);
        var sessionId = UUID.fromString(jsonResponse.get("sessionId").asText());

        // Assert
        assertSessionIsSaved(sessionId, challenge);
    }

    private void createAndActivateUser() {
        var user = User.createNew(new EmailAddress(DEFAULT_EMAIL));
        user.activate();
        userRepository.save(user);
    }

    private void assertUserIsCreatedAndEnabled() {
        var emailObj = new EmailAddress(DEFAULT_EMAIL);
        var userOptional = userRepository.findByEmail(emailObj);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.isEnabled()).isTrue());
    }

    @Test
    void completeVerification_should_return_400_response_when_new_user_provide_invalid_otp() throws Exception {
        // Arrange
        var hashedOtp = hasher.hashSecurely(DEFAULT_OTP);
        var otp = Otp.createNew(DEFAULT_EMAIL, hashedOtp, clock, otpProperties.ttl());
        otpRepository.save(otp);

        var challenge = createFixedChallenge();
        Mockito.when(challengeGeneratorMock.generate()).thenReturn(challenge);

        var completeVerificationRequest = new CompleteVerificationRequest(DEFAULT_EMAIL, INVALID_OTP);

        // Act & Assert
        mockMvc.perform(post(VERIFICATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeVerificationRequest)))
                .andExpect(status().is4xxClientError());

        // Additional Assertions
        assertUserNotCreated();
    }

    private void assertUserNotCreated() {
        var userOpt = userRepository.findByEmail(new EmailAddress(DEFAULT_EMAIL));
        assertThat(userOpt).isEmpty();
    }

    private Challenge createFixedChallenge() {
        var challengeValue = new byte[]{123, 123};
        return new Challenge(challengeValue, CHALLENGE_TTL, FIXED_CLOCK);
    }

    private void assertSessionIsSaved(UUID expectedSessionId, Challenge challenge) {
        var loadedSessionOptional = sessionRepository.load(expectedSessionId);

        assertThat(loadedSessionOptional)
                .isPresent()
                .hasValueSatisfying(loadedSession -> {
                    assertThat(loadedSession.challenge()).isEqualTo(challenge);
                    assertThat(loadedSession.challenge().expirationTime()).isEqualTo(Instant.now(FIXED_CLOCK).plus(CHALLENGE_TTL));
                });
    }
}
