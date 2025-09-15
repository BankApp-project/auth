package bankapp.auth.infrastructure.driven.passkey.service;


import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.verifier.exception.BadSignatureException;
import com.webauthn4j.verifier.exception.MaliciousCounterValueException;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@ActiveProfiles("test")
class WebAuthnAuthenticationVerificationTest {

    public static final String RP_ID = "bankapp.online";
    @Autowired
    private WebAuthnVerificationService webAuthnService;

    private Session session;
    private TestPasskeyProvider.PasskeyInfo passkeyInfo;

    @BeforeEach
    void setup() {
        session = getSession();
        passkeyInfo = TestPasskeyProvider.createSamplePasskeyInfo();
    }

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_invalid_response() {
        // Arrange
        var invalidResponse = "this is not valid json";

        // Act & Assert
        assertThrows(DataConversionException.class,
                () -> webAuthnService.confirmAuthenticationChallenge(invalidResponse, session, passkeyInfo.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_signature_is_invalid() throws Exception {
        // Arrange: Create a passkey with one keypair, but sign with a different one.
        var maliciousKeyPair = WebAuthnTestHelper.generatePasskeyKeyPair(); // Generate a second, different keypair (keypair B)

        // Generate response using the wrong private key (private key B)
        var authenticationResponseJSON = WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                session.challenge().challenge(),
                RP_ID,
                uuidToBytes(passkeyInfo.passkey().getId()),
                maliciousKeyPair,
                passkeyInfo.passkey().getSignCount()
        );

        // Act & Assert: Verification should fail because the signature doesn't match the stored public key.
        assertThrows(BadSignatureException.class,
                () -> webAuthnService.confirmAuthenticationChallenge(authenticationResponseJSON, session, passkeyInfo.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_throw_when_sign_count_lower_than_the_one_in_passkey() throws Exception {

        var authenticationResponseJSON = WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                session.challenge().challenge(),
                RP_ID,
                passkeyInfo.credentialIdBytes(),
                passkeyInfo.keyPair(),
                passkeyInfo.passkey().getSignCount() - 1
        );

        assertThrows(MaliciousCounterValueException.class,
                () -> webAuthnService.confirmAuthenticationChallenge(authenticationResponseJSON, session, passkeyInfo.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_return_updated_Passkey_when_provided_valid_parameters() throws Exception {

        var authenticationResponseJSON = WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                session.challenge().challenge(),
                RP_ID,
                passkeyInfo.credentialIdBytes(), // Use the byte[] version of the ID here
                passkeyInfo.keyPair(),
                passkeyInfo.passkey().getSignCount() + 1
        );

        // Act
        var signCount = passkeyInfo.passkey().getSignCount();
        var updatedPasskey = webAuthnService.confirmAuthenticationChallenge(authenticationResponseJSON, session, passkeyInfo.passkey());

        // Assert
        assertNotNull(updatedPasskey);
        assertThat(updatedPasskey.getSignCount()).isGreaterThan(signCount);
    }

    /**
     * Helper to generate a Session object for tests.
     */
    private @NotNull Session getSession() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var sessionId = UUID.randomUUID();
        // Use a secure random challenge value for each test
        var challengeVal = new byte[32];
        new SecureRandom().nextBytes(challengeVal);

        return new Session(sessionId, new Challenge(challengeVal, TTL, FIXED_CLOCK), UUID.randomUUID());
    }
}
