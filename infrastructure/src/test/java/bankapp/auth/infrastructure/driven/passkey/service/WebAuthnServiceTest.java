package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import org.jetbrains.annotations.NotNull;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WebAuthnServiceTest {

    @Autowired
    private WebAuthnVerificationService webAuthnService;

    private static final ObjectConverter objectConverter = new ObjectConverter();
    private static final AttestationObjectConverter attObjConv = new AttestationObjectConverter(objectConverter);
    private static final CollectedClientDataConverter collCltDataConv = new CollectedClientDataConverter(objectConverter);


    @Test
    void confirmRegistrationChallenge_should_return_data_parsable_by_webauthn4j() throws Exception {
        // 1. ARRANGE
        // Get a challenge for the registration ceremony
        var challenge = getChallenge();

        // Use the helper to generate a valid client response based on the challenge
        var registrationResponseJSON = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challenge.challenge());


        // 2. ACT
        // Call the service method under test
        var passkeyRegistrationData = webAuthnService.confirmRegistrationChallenge(registrationResponseJSON, challenge);


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
        var challenge = getChallenge();

        var invalidResponse = "xoxoxo";

        assertThrows(RegistrationConfirmAttemptException.class, () -> webAuthnService.confirmRegistrationChallenge(invalidResponse, challenge));
    }

    @Test
    void confirmRegistrationChallenge_should_return_RegistrationData_when_provided_valid_parameters() throws Exception {
        var challenge = getChallenge();
        var registrationResponseJSON = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challenge.challenge());

        var res = webAuthnService.confirmRegistrationChallenge(registrationResponseJSON, challenge);

        assertNotNull(res);
        assertThat(res).usingRecursiveAssertion().hasNoNullFields().ignoringFields("transports", "extensions");
    }

    private @NotNull Session getChallenge() {
        final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
        final Duration TTL = Duration.ofSeconds(60);

        var sessionId = UUID.randomUUID();
        var challengeVal = new byte[] {123,111};
        return new Session(sessionId, challengeVal, TTL, FIXED_CLOCK, UUID.randomUUID());
    }

}