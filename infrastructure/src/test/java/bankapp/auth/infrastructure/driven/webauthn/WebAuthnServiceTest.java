package bankapp.auth.infrastructure.driven.webauthn;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.infrastructure.driven.webauthn.service.RegistrationConfirmAttemptException;
import bankapp.auth.infrastructure.driven.webauthn.service.WebAuthnService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WebAuthnServiceTest {

    private final WebAuthnService webAuthnService = new WebAuthnService();

    @Test
    void confirmRegistrationChallenge_should_throw_exception_when_invalid_response() {
        var challenge = getChallenge();

        var invalidResponse = "xoxoxo";

        assertThrows(RegistrationConfirmAttemptException.class, () -> webAuthnService.confirmRegistrationChallenge(invalidResponse, challenge));
    }

    private @NotNull Challenge getChallenge() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var challengeId = UUID.randomUUID();
        var challengeVal = new byte[] {123,111};
        return new Challenge(challengeId, challengeVal, TTL, FIXED_CLOCK);
    }

}