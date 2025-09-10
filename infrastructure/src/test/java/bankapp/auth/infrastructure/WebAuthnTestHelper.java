package bankapp.auth.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebAuthnTestHelper {

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();
    private static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());

    /**
     * Generates a complete, cryptographically valid RegistrationResponseJSON for testing purposes.
     * This simulates a client creating a new passkey.
     *
     * @param challenge The challenge bytes provided by the server's getPasskeyCreationOptions.
     *
     * @return A JSON string representing the full response from the client.
     * @throws Exception if any cryptographic or serialization error occurs.
     */
    public static String generateValidRegistrationResponseJSON(byte[] challenge) throws Exception {

        // --- 1. Define Constants ---
        String rpId = "bankapp.online";
        String origin = "https://bankapp.online";
        byte[] aaguid = new byte[16]; // AAGUID can be all zeros for testing

        // --- 2. Generate a new Key Pair for the Credential ---
        // Alg -7 is ES256, which uses the P-256 curve (secp256r1)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = kpg.generateKeyPair();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        byte[] credentialId = new byte[16];
        new SecureRandom().nextBytes(credentialId);

        // --- 3. Construct clientDataJSON ---
        Map<String, Object> clientData = new LinkedHashMap<>();
        clientData.put("type", "webauthn.create");
        clientData.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge));
        clientData.put("origin", origin);
        clientData.put("crossOrigin", false);

        byte[] clientDataJSONBytes = jsonMapper.writeValueAsBytes(clientData);
        byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJSONBytes);

        // --- 4. Construct Authenticator Data (authData) ---
        byte[] rpIdHash = MessageDigest.getInstance("SHA-256").digest(rpId.getBytes());
        byte flags = 0b01000101; // Sets UP (Bit 0), UV (Bit 2), and AT (Bit 6)
        byte[] signCount = {0, 0, 0, 0};

        // --- 4a. Construct Attested Credential Data ---
        Map<Integer, Object> cosePublicKey = new LinkedHashMap<>();
        cosePublicKey.put(1, 2); // kty: EC2
        cosePublicKey.put(3, -7); // alg: ES256
        cosePublicKey.put(-1, 1); // crv: P-256
        cosePublicKey.put(-2, publicKey.getW().getAffineX().toByteArray()); // x
        cosePublicKey.put(-3, publicKey.getW().getAffineY().toByteArray()); // y

        byte[] cosePublicKeyBytes = cborMapper.writeValueAsBytes(cosePublicKey);
        byte[] credentialIdLength = {0, (byte) credentialId.length};

        ByteArrayOutputStream attestedCredentialDataStream = new ByteArrayOutputStream();
        attestedCredentialDataStream.write(aaguid);
        attestedCredentialDataStream.write(credentialIdLength);
        attestedCredentialDataStream.write(credentialId);
        attestedCredentialDataStream.write(cosePublicKeyBytes);
        byte[] attestedCredentialData = attestedCredentialDataStream.toByteArray();

        // --- 4b. Combine to form authData ---
        ByteArrayOutputStream authDataStream = new ByteArrayOutputStream();
        authDataStream.write(rpIdHash);
        authDataStream.write(flags);
        authDataStream.write(signCount);
        authDataStream.write(attestedCredentialData);
        byte[] authData = authDataStream.toByteArray();

        // --- 5. Create Signature ---
        // The signature is over the concatenation of authData and the clientDataHash
        ByteArrayOutputStream signatureBaseStream = new ByteArrayOutputStream();
        signatureBaseStream.write(authData);
        signatureBaseStream.write(clientDataHash);
        byte[] signatureBase = signatureBaseStream.toByteArray();

        Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA");
        ecdsaSignature.initSign(keyPair.getPrivate());
        ecdsaSignature.update(signatureBase);
        byte[] signature = ecdsaSignature.sign();

        // --- 6. Construct Attestation Object (CBOR) ---
        Map<String, Object> attStmt = new LinkedHashMap<>();
        attStmt.put("alg", -7); // ES256
        attStmt.put("sig", signature);

        Map<String, Object> attestationObject = new LinkedHashMap<>();
        attestationObject.put("fmt", "packed");
        attestationObject.put("authData", authData);
        attestationObject.put("attStmt", attStmt);

        byte[] attestationObjectBytes = cborMapper.writeValueAsBytes(attestationObject);


        // --- 7. Assemble the final JSON response ---
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("clientDataJSON", Base64.getUrlEncoder().withoutPadding().encodeToString(clientDataJSONBytes));
        response.put("attestationObject", Base64.getUrlEncoder().withoutPadding().encodeToString(attestationObjectBytes));

        Map<String, Object> finalJson = new LinkedHashMap<>();
        finalJson.put("id", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        finalJson.put("rawId", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        finalJson.put("type", "public-key");
        finalJson.put("response", response);

        return jsonMapper.writeValueAsString(finalJson);
    }
}
