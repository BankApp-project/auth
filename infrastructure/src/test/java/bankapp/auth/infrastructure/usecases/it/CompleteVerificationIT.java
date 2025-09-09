package bankapp.auth.infrastructure.usecases.it;


import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.domain.model.Otp;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.WithPostgresContainer;
import bankapp.auth.infrastructure.WithRedisContainer;
import bankapp.auth.infrastructure.persistance.otp.config.OtpProperties;
import bankapp.auth.infrastructure.rest.verification.complete.dto.CompleteVerificationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-postgres")
@Transactional // Automatically rolls back database changes after each test
public class CompleteVerificationIT implements WithPostgresContainer, WithRedisContainer {

    private static final String VERIFICATION_COMPLETE_ENDPOINT = "/verification/complete/email/";
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
    private ChallengeRepository challengeRepository;

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
                .andExpect(jsonPath("$.challengeId").value(challenge.challengeId().toString()))
                .andExpect(jsonPath("$.registrationOptions.challenge").value(Base64.getEncoder().encodeToString(challenge.value())));

        // Additional Assertions
        assertChallengeIsSaved(challenge);
        assertUserIsCreatedAndDisabled();
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
                .andExpect(jsonPath("$.challengeId").value(challenge.challengeId().toString()))
                .andExpect(jsonPath("$.loginOptions.challenge").value(Base64.getEncoder().encodeToString(challenge.value())));

        // Additional Assertions
        assertChallengeIsSaved(challenge);
        assertUserIsCreatedAndEnabled();
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
        var challengeId = UUID.randomUUID();
        var challengeValue = new byte[]{123, 123};
        return new Challenge(challengeId, challengeValue, CHALLENGE_TTL, FIXED_CLOCK);
    }

    private void assertChallengeIsSaved(Challenge expectedChallenge) {
        var loadedChallengeOptional = challengeRepository.load(expectedChallenge.challengeId());

        assertThat(loadedChallengeOptional)
                .isPresent()
                .hasValueSatisfying(loadedChallenge -> {
                    assertThat(loadedChallenge.value()).isEqualTo(expectedChallenge.value());
                    assertThat(loadedChallenge.challengeId()).isEqualTo(expectedChallenge.challengeId());
                    assertThat(loadedChallenge.expirationTime()).isEqualTo(Instant.now(FIXED_CLOCK).plus(CHALLENGE_TTL));
                });
    }
}
