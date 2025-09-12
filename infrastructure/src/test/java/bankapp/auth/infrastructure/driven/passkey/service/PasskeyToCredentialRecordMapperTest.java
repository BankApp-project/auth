package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.ClientDataType;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.test.TestDataUtil;
import com.webauthn4j.util.MessageDigestUtil;
import com.webauthn4j.util.UUIDUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasskeyToCredentialRecordMapperTest {

    private static final ObjectConverter objConv = new ObjectConverter();
    private static final AttestationObjectConverter attestationObjConv = new AttestationObjectConverter(objConv);
    private static final CollectedClientDataConverter clientDataConv = new CollectedClientDataConverter(objConv);

    @Test
    void from_should_create_valid_credentialRecord_from_Passkey() {
        // ARRANGE: Create a valid source Passkey object with realistic data.
        var testPasskey = createSamplePasskey();

        // ACT: Map the Passkey to a CredentialRecord.
        var res = PasskeyToCredentialRecordMapper.from(testPasskey);

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

    /**
     * Creates a sample Passkey object populated with cryptographically valid and
     * internally consistent data using the webauthn4j-test utility library.
     * <p>
     * This implementation uses the precise, recommended factory methods for each
     * component, ensuring the test data is as realistic as possible.
     *
     * @return A Passkey instance with valid data.
     */
    private Passkey createSamplePasskey() {
        // STEP 1: Create the CollectedClientData for a registration ceremony.
        // We must specify the type as `WEBAUTHN_CREATE`.
        CollectedClientData clientData = TestDataUtil.createClientData(ClientDataType.WEBAUTHN_CREATE);
        byte[] clientDataJSONBytes = clientDataConv.convertToBytes(clientData);

        // STEP 2: Calculate the hash of the client data.
        // This is required to create a properly signed AttestationObject.
        byte[] clientDataHash = MessageDigestUtil.createSHA256().digest(clientDataJSONBytes);

        // STEP 3: Create a signed AttestationObject using the client data hash.
        // This factory method simulates a real authenticator signing both its own data
        // and the client data hash, creating a valid cryptographic binding.
        AttestationObject attestationObject = TestDataUtil.createAttestationObjectWithBasicPackedECAttestationStatement(clientDataHash);
        byte[] attestationObjectBytes = attestationObjConv.convertToBytes(attestationObject);

        // STEP 4: Extract all necessary data from the generated objects.
        // Because the objects are created together, all the internal data is consistent.
        var authenticatorData = attestationObject.getAuthenticatorData();
        var attestedCredentialData = authenticatorData.getAttestedCredentialData();

        Assertions.assertNotNull(attestedCredentialData);
        byte[] credentialIdBytes = attestedCredentialData.getCredentialId();
        UUID credentialId = UUIDUtil.fromBytes(credentialIdBytes);

        Assertions.assertNotNull(attestedCredentialData.getCOSEKey().getPublicKey());
        byte[] publicKeyBytes = attestedCredentialData.getCOSEKey().getPublicKey().getEncoded();

        long signCount = authenticatorData.getSignCount();
        boolean uvInitialized = authenticatorData.isFlagUV();
        boolean backupEligible = authenticatorData.isFlagBE();
        boolean backupState = authenticatorData.isFlagBS();

        // For the stub, we can generate a new random user ID.
        var userId = UUID.randomUUID();

        // STEP 5: Construct the final Passkey object.
        return new Passkey(
                credentialId,
                userId,
                PublicKeyCredentialType.PUBLIC_KEY.getValue(), // "public-key"
                publicKeyBytes,
                signCount,
                uvInitialized,
                backupEligible,
                backupState,
                Arrays.asList(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.HYBRID),
                Collections.singletonMap("someKey", "someVal"),
                attestationObjectBytes,
                clientDataJSONBytes
        );
    }
}