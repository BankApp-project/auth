package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.infrastructure.utils.PasskeyTestProvider;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.util.UUIDUtil;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasskeyToCredentialRecordMapperTest {


    @Test
    void from_should_create_valid_credentialRecord_from_Passkey() {
        // ARRANGE: Create a valid source Passkey object with realistic data.
        var testPasskey = PasskeyTestProvider.createSamplePasskey();

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
        assertArrayEquals(UUIDUtil.convertUUIDToBytes(testPasskey.getId()), res.getAttestedCredentialData().getCredentialId(), "Credential ID should match.");

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
        assertEquals(Set.of(testPasskey.getTransports()), res.getTransports(), "Transports should match.");
    }
}