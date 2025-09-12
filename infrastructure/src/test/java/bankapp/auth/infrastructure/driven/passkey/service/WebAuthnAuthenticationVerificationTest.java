package bankapp.auth.infrastructure.driven.passkey.service;


import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static bankapp.auth.application.shared.service.ByteArrayUtil.uuidToBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class WebAuthnAuthenticationVerificationTest {

    @Autowired
    private WebAuthnVerificationService webAuthnService;

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_invalid_response() {
        // Arrange
        var challenge = getChallenge();
        var registeredPasskey = getRegisteredPasskey(); // A valid passkey is required
        var invalidResponse = "this is not valid json";

        // Act & Assert
        assertThrows(AuthenticationConfirmAttemptException.class,
                () -> webAuthnService.confirmAuthenticationChallenge(invalidResponse, challenge, registeredPasskey.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_signature_is_invalid() throws Exception {
        // Arrange: Create a passkey with one keypair, but sign with a different one.
        var challenge = getChallenge();
        var passkeyInfo = getRegisteredPasskey(); // This contains the valid passkey data (public key A)
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
        var passkeyInfo = getRegisteredPasskey(); // Contains Passkey object, KeyPair, and credentialId bytes
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
     * Helper to generate a Challenge object for tests.
     */
    private @NotNull Challenge getChallenge() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var challengeId = UUID.randomUUID();
        // Use a secure random challenge value for each test
        var challengeVal = new byte[32];
        new SecureRandom().nextBytes(challengeVal);

        return new Challenge(challengeId, challengeVal, TTL, FIXED_CLOCK, UUID.randomUUID());
    }


    /**
     * Helper to generate a consistent Passkey object and the corresponding KeyPair needed for signing.
     * This method now correctly populates all fields of the detailed Passkey domain object.
     */
    private @NotNull PasskeyInfo getRegisteredPasskey() {
        try {
            // 1. Generate the cryptographic key pair for the passkey.
            var keyPair = WebAuthnTestHelper.generatePasskeyKeyPair();

            // 2. Generate required identifiers.
            var passkeyId = UUID.randomUUID();
            var userHandle = UUID.randomUUID();

            // 3. Generate the actual COSE public key that corresponds to the keyPair.
            //    This is what your server would store in the database.
            byte[] publicKeyCose = WebAuthnTestHelper.generateCosePublicKeyBytes(keyPair);

            // 4. Create the Passkey domain object with realistic default values for a new registration.
            var passkey = new Passkey(
                    passkeyId,
                    userHandle,
                    "public-key",
                    publicKeyCose,
                    0L, // A new passkey always starts with a signature count of 0.
                    true, // Assume user was verified during registration.
                    true,
                    true, // Assume the passkey is backed up.
                    List.of(AuthenticatorTransport.INTERNAL),// Default transport.
                    Map.of("SomeKey", "SomeVal"),
                    "attestationObj".getBytes(),
                    "clientData".getBytes()
            );

            // 5. Return the Passkey object and the KeyPair needed to sign future challenges.
            //    The credentialId for the JSON helper must be derived from the Passkey's UUID.
            return new PasskeyInfo(passkey, keyPair, uuidToBytes(passkey.getId()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate registered passkey test data", e);
        }
    }

    // A simple record to hold both the mocked DB entity and the keypair for the test.
    private record PasskeyInfo(Passkey passkey, KeyPair keyPair, byte[] credentialIdBytes) {
    }
}
