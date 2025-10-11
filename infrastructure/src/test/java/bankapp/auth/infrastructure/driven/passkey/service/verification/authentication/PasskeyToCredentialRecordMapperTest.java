package bankapp.auth.infrastructure.driven.passkey.service.verification.authentication;

import bankapp.auth.domain.model.enums.AuthenticatorTransport;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import com.webauthn4j.converter.util.ObjectConverter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PasskeyToCredentialRecordMapperTest {


    @Test
    void from_should_create_valid_credentialRecord_from_Passkey() {
        // ARRANGE: Create a valid source Passkey object with realistic data.
        var testPasskey = TestPasskeyProvider.createSamplePasskey();

        // ACT: Map the Passkey to a CredentialRecord.
        var mapper = new PasskeyToCredentialRecordMapper(new WebAuthnMapper(new ObjectConverter()));
        var res = mapper.from(testPasskey);

        // ASSERT: Verify that all fields have been mapped correctly.
        assertNotNull(res, "The resulting CredentialRecord should not be null.");

        // --- Assert Authenticator Flags and Counter ---
        assertEquals(testPasskey.isBackupState(), res.isBackedUp(), "Backup state flag should match.");
        assertEquals(testPasskey.isBackupEligible(), res.isBackupEligible(), "Backup eligible flag should match.");
        assertEquals(testPasskey.isUvInitialized(), res.isUvInitialized(), "UV initialized flag should match.");
        assertEquals(testPasskey.getSignCount(), res.getCounter(), "Signature counter should match.");

        // --- Assert Core Credential Data (using assertArrayEquals for byte arrays) ---
        assertNotNull(res.getAttestedCredentialData(), "AttestedCredentialData should not be null.");
        assertArrayEquals(testPasskey.getId(), res.getAttestedCredentialData().getCredentialId(), "Credential ID should match.");

        // Correctly compare the raw COSE public key bytes.
        // The `getPublicKey().getEncoded()` method returns a different format (X.509),
        // so we must compare the raw bytes stored in the COSEKey object.
        assertNotNull(res.getAttestedCredentialData().getCOSEKey().getPublicKey(), "Public Key should not be null.");
        assertArrayEquals(testPasskey.getPublicKey(), res.getAttestedCredentialData().getCOSEKey().getPublicKey().getEncoded(), "Public key bytes should match.");

        // --- Assert Raw Data Objects ---
        assertNotNull(res.getAttestedCredentialData(), "AttestationObject should not be null.");

        assertNotNull(res.getClientData(), "ClientData should not be null.");

        // --- Assert Other Metadata ---
        assertNotNull(res.getTransports(), "Transports set should not be null.");
        assertEquals(getSetOfStrings(testPasskey.getTransports()), getSetOfStrings(res.getTransports()), "Transports should match.");
    }

    private Set<String> getSetOfStrings(Set<com.webauthn4j.data.AuthenticatorTransport> transports) {
        assertNotNull(transports);
        return transports.stream()
                .map(com.webauthn4j.data.AuthenticatorTransport::getValue)
                .collect(Collectors.toSet());
    }

    private Set<String> getSetOfStrings(List<AuthenticatorTransport> transports) {
        return transports.stream()
                .map(AuthenticatorTransport::getValue)
                .collect(Collectors.toSet());
    }
}