package bankapp.auth.infrastructure.driven.passkey.service;


import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static bankapp.auth.application.shared.service.ByteArrayUtil.uuidToBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
class WebAuthnAuthenticationVerificationTest {

    @Autowired
    private WebAuthnVerificationService webAuthnService;

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_invalid_response() {
        // Arrange
        var challenge = getChallenge();
        var registeredPasskey = TestPasskeyProvider.createSamplePasskeyInfo(); // A valid passkey is required
        var invalidResponse = "this is not valid json";

        // Act & Assert
        assertThrows(AuthenticationConfirmAttemptException.class,
                () -> webAuthnService.confirmAuthenticationChallenge(invalidResponse, challenge, registeredPasskey.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_signature_is_invalid() throws Exception {
        // Arrange: Create a passkey with one keypair, but sign with a different one.
        var challenge = getChallenge();
        var passkeyInfo = TestPasskeyProvider.createSamplePasskeyInfo(); // This contains the valid passkey data (public key A)
        var maliciousKeyPair = WebAuthnTestHelper.generatePasskeyKeyPair(); // Generate a second, different keypair (keypair B)

        // Generate response using the wrong private key (private key B)
        var authenticationResponseJSON = WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                challenge.value(),
                "bankapp.online", // rpId must match
                uuidToBytes(passkeyInfo.passkey().getId()),
                maliciousKeyPair
        );

        // Act & Assert: Verification should fail because the signature doesn't match the stored public key.
        assertThrows(AuthenticationConfirmAttemptException.class,
                () -> webAuthnService.confirmAuthenticationChallenge(authenticationResponseJSON, challenge, passkeyInfo.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_return_updated_Passkey_when_provided_valid_parameters() throws Exception {
        // Arrange
        var challenge = getChallenge();
        var passkeyInfo = TestPasskeyProvider.createSamplePasskeyInfo(); // Contains Passkey object, KeyPair, and credentialId bytes
        var rpId = "bankapp.online";

        var authenticationResponseJSON = WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                challenge.value(),
                rpId,
                passkeyInfo.credentialIdBytes(), // Use the byte[] version of the ID here
                passkeyInfo.keyPair()
        );

        // Act
        var signCount = passkeyInfo.passkey().getSignCount();
        var updatedPasskey = webAuthnService.confirmAuthenticationChallenge(authenticationResponseJSON, challenge, passkeyInfo.passkey());

        // Assert
        assertNotNull(updatedPasskey);
        assertThat(updatedPasskey.getSignCount()).isGreaterThan(signCount);
    }

    /**
     * Helper to generate a Session object for tests.
     */
    private @NotNull Session getChallenge() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var sessionId = UUID.randomUUID();
        // Use a secure random challenge value for each test
        var challengeVal = new byte[32];
        new SecureRandom().nextBytes(challengeVal);

        return new Session(sessionId, challengeVal, TTL, FIXED_CLOCK, UUID.randomUUID());
    }
}
