package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.infrastructure.WebAuthnTestHelper;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.util.UUIDUtil;
import jakarta.validation.constraints.NotNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegistrationDataMapperTest {

    private RegistrationDataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RegistrationDataMapper();
    }

    @Test
    void toDomainEntity_shouldCorrectlyMapAllFields() throws Exception {
        // ARRANGE
        // 1. Generate a realistic WebAuthn client response using our helper
        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        String registrationResponseJson = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challengeBytes);

        // 2. Use the standard webauthn4j manager to parse it into a source RegistrationData object
        WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
        RegistrationData registrationData = webAuthnManager.parseRegistrationResponseJSON(registrationResponseJson);

        // 3. Define the server-side context (the user's ID) that the mapper also needs
        UUID userId = UUID.randomUUID();

        // ACT
        // Call the method under test with the new signature
        PasskeyRegistrationData result = mapper.toDomainEntity(registrationData, userId);

        // ASSERT
        // This test will fail until the mapper is implemented.
        // It serves as the precise specification for the mapping logic.

        assertThat(result).isNotNull();

        // Extract source data for easier comparison
        assertNotNull(registrationData.getAttestationObject());
        var authData = registrationData.getAttestationObject().getAuthenticatorData();
        AttestedCredentialData attestedCredData = authData.getAttestedCredentialData();

        // === Assert Core Fields ===
        assertNotNull(attestedCredData);
        assertThat(result.id()).isEqualTo(UUIDUtil.fromBytes(attestedCredData.getCredentialId()));
        assertThat(result.userHandle()).isEqualTo(userId); // Assert that the passed-in userId is mapped
        assertThat(result.type()).isEqualTo("public-key");
        assertNotNull(attestedCredData.getCOSEKey().getPublicKey());
        assertThat(result.publicKey()).isEqualTo(attestedCredData.getCOSEKey().getPublicKey().getEncoded());
        assertThat(result.signCount()).isEqualTo(authData.getSignCount());
        assertThat(result.uvInitialized()).isEqualTo(authData.isFlagUV());

        // === Assert Optional Flags and Metadata ===
        assertThat(result.backupEligible()).isEqualTo(authData.isFlagBE());
        assertThat(result.backupState()).isEqualTo(authData.isFlagBS());

        //these two needs converter
        Assertions.assertThat(result.transports()).containsExactlyInAnyOrderElementsOf(getTransports(registrationData));
        assertNotNull(registrationData.getClientExtensions());
        assertThat(result.extensions()).isEqualTo(registrationData.getClientExtensions().getExtensions());

        // === Assert Attestation Data ===
        assertThat(result.attestationObject()).isEqualTo(registrationData.getAttestationObjectBytes());
        assertThat(result.attestationClientDataJSON()).isEqualTo(registrationData.getCollectedClientDataBytes());
    }

    private List<AuthenticatorTransport> getTransports(@NotNull RegistrationData registrationData) {
        var transports = Objects.requireNonNull(registrationData.getTransports(), "Transports cannot be null");

        if (transports.isEmpty()) {
            return Collections.emptyList();
        }

        return transports.stream()
                .map(t -> AuthenticatorTransport.fromValue(t.getValue()))
                .toList();
    }
}
