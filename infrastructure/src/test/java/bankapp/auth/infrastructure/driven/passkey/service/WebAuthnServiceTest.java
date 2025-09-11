package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.infrastructure.WebAuthnTestHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
class WebAuthnServiceTest {

    @Autowired
    private WebAuthnVerificationService webAuthnService;

    @Test
    void confirmRegistrationChallenge_should_throw_exception_when_invalid_response() {
        var challenge = getChallenge();

        var invalidResponse = "xoxoxo";

        assertThrows(RegistrationConfirmAttemptException.class, () -> webAuthnService.confirmRegistrationChallenge(invalidResponse, challenge));
    }

    @Test
    void confirmRegistrationChallenge_should_return_RegistrationData_when_provided_valid_parameters() throws Exception {
        var challenge = getChallenge();
        var registrationResponseJSON = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challenge.value());

        var res = webAuthnService.confirmRegistrationChallenge(registrationResponseJSON, challenge);

        assertNotNull(res);
        assertThat(res).usingRecursiveAssertion().hasNoNullFields().ignoringFields("transports", "extensions");
    }

    private @NotNull Challenge getChallenge() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var challengeId = UUID.randomUUID();
        var challengeVal = new byte[] {123,111};
        return new Challenge(challengeId, challengeVal, TTL, FIXED_CLOCK, UUID.randomUUID());
    }

}