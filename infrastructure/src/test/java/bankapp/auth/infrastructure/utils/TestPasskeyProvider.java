package bankapp.auth.infrastructure.utils;

import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.annotations.Nullable;
import bankapp.auth.domain.model.enums.AuthenticatorTransport;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.AuthenticatorDataConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.ClientDataType;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.test.TestAttestationStatementUtil;
import com.webauthn4j.test.TestAttestationUtil;
import com.webauthn4j.test.TestDataUtil;
import com.webauthn4j.util.ECUtil;
import com.webauthn4j.util.MessageDigestUtil;
import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.*;

public class TestPasskeyProvider {

    private static final ObjectConverter objConv = new ObjectConverter();
    private static final AttestationObjectConverter attestationObjConv = new AttestationObjectConverter(objConv);
    private static final CollectedClientDataConverter clientDataConv = new CollectedClientDataConverter(objConv);
    private static final AuthenticatorDataConverter authenticatorDataConverter = new AuthenticatorDataConverter(objConv);

    public static Passkey createSamplePasskey() {
        return createSamplePasskey(null);
    }

    /// Creates a sample Passkey object populated with cryptographically valid and
    /// internally consistent data using the webauthn4j-test utility library.
    ///
    /// This implementation uses the precise, recommended factory methods for each
    /// component, ensuring the test data is as realistic as possible.
    ///
    /// Known issue:
    /// - This method will create 16 bytes long credentialId, but attestationObjectBytes will be 32 bytes long.
    ///   Their values are same (0's), so it's important, to only validate
    ///   first 16 bytes of credentialId from attestationObjectBytes in test suite
    ///
    /// @return A Passkey instance with valid data.
    public static Passkey createSamplePasskey(@Nullable UUID userId) {
        // For the stub, we can generate a new random user ID or use provided one.
        userId = userId == null ? UUID.randomUUID() : userId;

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
        // Because of TestDataUtil impl of new data creation, it will always be 32x0's
        byte[] credentialIdBytes = attestedCredentialData.getCredentialId();

        Assertions.assertNotNull(attestedCredentialData.getCOSEKey().getPublicKey());
        byte[] publicKeyBytes = attestedCredentialData.getCOSEKey().getPublicKey().getEncoded();

        long signCount = authenticatorData.getSignCount();
        boolean uvInitialized = authenticatorData.isFlagUV();
        boolean backupEligible = authenticatorData.isFlagBE();
        boolean backupState = authenticatorData.isFlagBS();

        // STEP 5: Construct the final Passkey object.
        return new Passkey(
                credentialIdBytes,
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

    public static PasskeyInfo createSamplePasskeyInfo() {
        return createSamplePasskeyInfo(null);
    }

    /**
     * Creates a complete and cryptographically consistent Passkey test data set.
     * <p>
     * This method constructs a valid AttestationObject containing a new public key
     * while retaining the corresponding KeyPair. This allows the private key to be
     * used later for signing authentication challenges.
     *
     * @return A PasskeyInfo record containing the generated Passkey, its KeyPair, and the raw credential ID.
     */
    public static PasskeyInfo createSamplePasskeyInfo(@Nullable UUID userId) {
        //generate userId if not provided
        userId = userId == null ? UUID.randomUUID() : userId;

        // STEP 1: Generate the user's cryptographic key pair. This is the key that must be
        // retained to sign future authentication challenges.
        KeyPair userKeyPair = ECUtil.createKeyPair();
        ECPublicKey userPublicKey = (ECPublicKey) userKeyPair.getPublic();

        // STEP 2: Create standard client data for a registration ceremony.
        CollectedClientData clientData = TestDataUtil.createClientData(ClientDataType.WEBAUTHN_CREATE);
        byte[] clientDataJSONBytes = clientDataConv.convertToBytes(clientData);
        byte[] clientDataHash = MessageDigestUtil.createSHA256().digest(clientDataJSONBytes);

        // STEP 3: Create the AuthenticatorData, embedding the user's public key.
        COSEKey coseKey = TestDataUtil.createEC2COSEPublicKey(userPublicKey);
        AuthenticatorData<RegistrationExtensionAuthenticatorOutput> authenticatorData = TestDataUtil.createAuthenticatorData(coseKey);
        byte[] authenticatorDataBytes = authenticatorDataConverter.convert(authenticatorData);

        // STEP 4: Create a valid attestation signature. For a "packed" attestation, this signature
        // is created by a trusted attestation key, not the user's key. We simulate this
        // by using the test attestation private key provided by webauthn4j-test.
        PrivateKey attestationPrivateKey = TestAttestationUtil.load3tierTestAuthenticatorAttestationPrivateKey();
        byte[] signedData = ByteBuffer.allocate(authenticatorDataBytes.length + clientDataHash.length)
                .put(authenticatorDataBytes).put(clientDataHash).array();
        byte[] signature = TestDataUtil.calculateSignature(attestationPrivateKey, signedData);

        // STEP 5: Create the attestation statement with the signature.
        AttestationStatement attestationStatement = TestAttestationStatementUtil.createBasicPackedAttestationStatement(
                COSEAlgorithmIdentifier.ES256, signature);

        // STEP 6: Assemble the final AttestationObject.
        AttestationObject attestationObject = new AttestationObject(authenticatorData, attestationStatement);
        byte[] attestationObjectBytes = attestationObjConv.convertToBytes(attestationObject);

        // STEP 7: Extract data necessary for the Passkey domain object.
        AttestedCredentialData attestedCredentialData = authenticatorData.getAttestedCredentialData();
        Objects.requireNonNull(attestedCredentialData);
        byte[] credentialIdBytes = attestedCredentialData.getCredentialId();
        byte[] publicKeyCoseBytes = coseKey.getPublicKey().getEncoded();

        // STEP 8: Construct the final Passkey domain object.
        var passkey = new Passkey(
                credentialIdBytes,
                userId,
                PublicKeyCredentialType.PUBLIC_KEY.getValue(),
                publicKeyCoseBytes,
                authenticatorData.getSignCount(),
                authenticatorData.isFlagUV(),
                authenticatorData.isFlagBE(),
                authenticatorData.isFlagBS(),
                List.of(AuthenticatorTransport.INTERNAL),
                Collections.emptyMap(),
                attestationObjectBytes,
                clientDataJSONBytes
        );

        // STEP 9: Return the complete info bundle.
        return new PasskeyInfo(passkey, userKeyPair, credentialIdBytes);
    }

    /**
     * A record to hold a generated Passkey, the KeyPair used to create it,
     * and its raw credential ID for use in test responses.
     */
    public record PasskeyInfo(Passkey passkey, KeyPair keyPair, byte[] credentialIdBytes) {
    }
}
