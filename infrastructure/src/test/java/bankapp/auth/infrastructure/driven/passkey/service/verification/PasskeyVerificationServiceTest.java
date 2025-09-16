package bankapp.auth.infrastructure.driven.passkey.service.verification;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.infrastructure.driven.passkey.exception.AuthenticationConfirmAttemptException;
import bankapp.auth.infrastructure.driven.passkey.exception.RegistrationConfirmAttemptException;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static bankapp.auth.application.shared.service.ByteArrayUtil.uuidToBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PasskeyVerificationServiceTest {

    public static final String RP_ID = "bankapp.online";
    
    @Autowired
    private PasskeyVerificationService passkeyVerificationService;

    private static final ObjectConverter objectConverter = new ObjectConverter();
    private static final AttestationObjectConverter attObjConv = new AttestationObjectConverter(objectConverter);
    private static final CollectedClientDataConverter collCltDataConv = new CollectedClientDataConverter(objectConverter);

    private Session session;
    private TestPasskeyProvider.PasskeyInfo passkeyInfo;

    @BeforeEach
    void setup() {
        session = getSession();
        passkeyInfo = TestPasskeyProvider.createSamplePasskeyInfo();
    }


    // ===== Authentication Tests =====

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_invalid_response() {
        // Arrange
        var invalidResponse = "this is not valid json";

        // Act & Assert
        assertThrows(AuthenticationConfirmAttemptException.class,
                () -> passkeyVerificationService.handleAuthentication(invalidResponse, session, passkeyInfo.passkey()));
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
        assertThrows(AuthenticationConfirmAttemptException.class,
                () -> passkeyVerificationService.handleAuthentication(authenticationResponseJSON, session, passkeyInfo.passkey()));
    }

    @Test
    void confirmAuthenticationChallenge_should_throw_exception_when_sign_count_lower_than_the_one_in_passkey() throws Exception {

        var authenticationResponseJSON = WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                session.challenge().challenge(),
                RP_ID,
                passkeyInfo.credentialIdBytes(),
                passkeyInfo.keyPair(),
                passkeyInfo.passkey().getSignCount() - 1
        );

        assertThrows(MaliciousCounterException.class,
                () -> passkeyVerificationService.handleAuthentication(authenticationResponseJSON, session, passkeyInfo.passkey()));
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
        var updatedPasskey = passkeyVerificationService.handleAuthentication(authenticationResponseJSON, session, passkeyInfo.passkey());

        // Assert
        assertNotNull(updatedPasskey);
        assertThat(updatedPasskey.getSignCount()).isGreaterThan(signCount);
    }

    // ===== Registration Tests =====

    @Test
    void confirmRegistrationChallenge_should_return_data_parsable_by_webauthn4j() throws Exception {
        // 1. ARRANGE
        // Get a challenge for the registration ceremony
        var session = getSession();
        var challenge = session.challenge();

        // Use the helper to generate a valid client response based on the challenge
        var registrationResponseJSON = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challenge.challenge());


        // 2. ACT
        // Call the service method under test
        var passkeyRegistrationData = passkeyVerificationService.handleRegistration(registrationResponseJSON, session);


        // 3. ASSERT
        // First, perform basic checks to ensure the data is present
        assertNotNull(passkeyRegistrationData);
        assertNotNull(passkeyRegistrationData.getAttestationObject(), "AttestationObject bytes should not be null");
        assertTrue(passkeyRegistrationData.getAttestationObject().length > 0, "AttestationObject bytes should not be empty");
        assertNotNull(passkeyRegistrationData.getAttestationClientDataJSON(), "CollectedClientDataJSON bytes should not be null");
        assertTrue(passkeyRegistrationData.getAttestationClientDataJSON().length > 0, "CollectedClientDataJSON bytes should not be empty");


        // --- Core Integrity Check ---
        // Assert that the raw byte arrays can be deserialized back into their standard webauthn4j object representations

        // a) Parse the Attestation Object
        AttestationObject attestationObject = attObjConv.convert(passkeyRegistrationData.getAttestationObject());
        assertNotNull(attestationObject, "Parsed AttestationObject should not be null");
        assertNotNull(attestationObject.getAuthenticatorData(), "AuthenticatorData within the AttestationObject should not be null");
        assertEquals("packed", attestationObject.getFormat(), "Attestation format should be 'packed'");


        // b) Parse the Collected Client Data
        CollectedClientData collectedClientData = collCltDataConv.convert(passkeyRegistrationData.getAttestationClientDataJSON());
        assertNotNull(collectedClientData, "Parsed CollectedClientData should not be null");


        // c) Validate the content of the parsed client data
        // This is a critical check to ensure you processed the correct challenge response
        assertEquals("webauthn.create", collectedClientData.getType().getValue());
        assertEquals(new DefaultChallenge(challenge.challenge()), collectedClientData.getChallenge());
    }

    @Test
    void confirmRegistrationChallenge_should_throw_exception_when_invalid_response() {
        var challenge = getSession();

        var invalidResponse = "xoxoxo";

        assertThrows(RegistrationConfirmAttemptException.class, () -> passkeyVerificationService.handleRegistration(invalidResponse, challenge));
    }

    @Test
    void confirmRegistrationChallenge_should_return_RegistrationData_when_provided_valid_parameters() throws Exception {
        var session = getSession();
        var challenge = session.challenge();
        var registrationResponseJSON = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challenge.challenge());

        var res = passkeyVerificationService.handleRegistration(registrationResponseJSON, session);

        assertNotNull(res);
        assertThat(res).usingRecursiveAssertion().hasNoNullFields().ignoringFields("transports", "extensions");
    }

    private @NotNull Session getSession() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var sessionId = UUID.randomUUID();
        var challengeVal = new byte[] {123,111};
        return new Session(sessionId, new Challenge(challengeVal, TTL, FIXED_CLOCK), UUID.randomUUID());
    }
}