package bankapp.auth.infrastructure.utils;

import bankapp.auth.application.shared.service.ByteArrayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

public class WebAuthnTestHelper {

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();
    private static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());

    /**
     * Generates a new EC KeyPair using the P-256 curve, suitable for passkey credentials.
     *
     * @return A newly generated KeyPair.
     * @throws NoSuchAlgorithmException           if the EC algorithm is not available.
     * @throws InvalidAlgorithmParameterException if the secp256r1 curve parameter is invalid.
     */
    public static KeyPair generatePasskeyKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // Alg -7 is ES256, which uses the P-256 curve (secp256r1)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        return kpg.generateKeyPair();
    }

    /**
     * Generates a CBOR-encoded COSE (CBOR Object Signing and Encryption) public key
     * from a given EC KeyPair. This is the standard format for WebAuthn public keys
     * that should be stored by the Relying Party (your server).
     *
     * @param keyPair The KeyPair containing the elliptic curve public key to encode.
     *
     * @return A byte array representing the COSE public key.
     * @throws JsonProcessingException if the CBOR serialization process fails.
     */
    public static byte[] generateCosePublicKeyBytes(KeyPair keyPair) throws JsonProcessingException {
        // 1. Get the public key from the KeyPair and cast it to an Elliptic Curve public key.
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        // 2. Build the COSE key map according to RFC 8152.
        // For WebAuthn, we use integer keys for compactness.
        Map<Integer, Object> cosePublicKey = new LinkedHashMap<>();

        // Key Type (kty): 1 -> 2 (Elliptic Curve Keys)
        cosePublicKey.put(1, 2);

        // Algorithm (alg): 3 -> -7 (ES256 - ECDSA with SHA-256)
        cosePublicKey.put(3, -7);

        // Curve (crv): -1 -> 1 (P-256)
        cosePublicKey.put(-1, 1);

        // X-coordinate (x): -2 -> The raw x-coordinate bytes of the public key.
        cosePublicKey.put(-2, publicKey.getW().getAffineX().toByteArray());

        // Y-coordinate (y): -3 -> The raw y-coordinate bytes of the public key.
        cosePublicKey.put(-3, publicKey.getW().getAffineY().toByteArray());

        // 3. Serialize the map into a CBOR byte array.
        return cborMapper.writeValueAsBytes(cosePublicKey);
    }

    /**
     * Generates a complete, cryptographically valid RegistrationResponseJSON for testing purposes.
     * This version is a convenience method that generates a new random KeyPair and credentialId internally.
     *
     * @param challenge The challenge bytes provided by the server's getPasskeyCreationOptions.
     * @return A JSON string representing the full response from the client.
     * @throws Exception if any cryptographic or serialization error occurs.
     */
    public static String generateValidRegistrationResponseJSON(byte[] challenge) throws Exception {
        KeyPair keyPair = generatePasskeyKeyPair();
        byte[] credentialId = new byte[16];
        new SecureRandom().nextBytes(credentialId);
        return generateValidRegistrationResponseJSON(challenge, credentialId, keyPair);
    }

    /**
     * Generates a complete, cryptographically valid RegistrationResponseJSON using a provided KeyPair and credentialId.
     * This is useful for tests where the generated private key needs to be retained for a subsequent authentication test.
     *
     * @param challenge    The challenge bytes from the server.
     * @param credentialId The credential ID to be associated with the new passkey.
     * @param keyPair      The KeyPair to be embedded in the passkey. The private key will be used to sign the attestation.
     *
     * @return A JSON string representing the full response from the client.
     * @throws Exception if any cryptographic or serialization error occurs.
     */
    public static String generateValidRegistrationResponseJSON(byte[] challenge, byte[] credentialId, KeyPair keyPair) throws Exception {
        // --- 1. Define Constants ---
        String rpId = "bankapp.online";
        String origin = "https://bankapp.online";
        byte[] aaguid = new byte[16]; // AAGUID can be all zeros for testing
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        // --- 2. Construct clientDataJSON ---
        Map<String, Object> clientData = new LinkedHashMap<>();
        clientData.put("type", "webauthn.create");
        clientData.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge));
        clientData.put("origin", origin);
        clientData.put("crossOrigin", false);

        byte[] clientDataJSONBytes = jsonMapper.writeValueAsBytes(clientData);
        byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJSONBytes);

        // --- 3. Construct Authenticator Data (authData) ---
        byte[] rpIdHash = MessageDigest.getInstance("SHA-256").digest(rpId.getBytes());
        byte flags = 0b01000101; // Sets UP (Bit 0), UV (Bit 2), and AT (Bit 6)
        byte[] signCount = {0, 0, 0, 0};

        // --- 3a. Construct Attested Credential Data ---
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

        // --- 3b. Combine to form authData ---
        ByteArrayOutputStream authDataStream = new ByteArrayOutputStream();
        authDataStream.write(rpIdHash);
        authDataStream.write(flags);
        authDataStream.write(signCount);
        authDataStream.write(attestedCredentialData);
        byte[] authData = authDataStream.toByteArray();

        // --- 4. Create Signature ---
        ByteArrayOutputStream signatureBaseStream = new ByteArrayOutputStream();
        signatureBaseStream.write(authData);
        signatureBaseStream.write(clientDataHash);
        byte[] signatureBase = signatureBaseStream.toByteArray();

        Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA");
        ecdsaSignature.initSign(keyPair.getPrivate());
        ecdsaSignature.update(signatureBase);
        byte[] signature = ecdsaSignature.sign();

        // --- 5. Construct Attestation Object (CBOR) ---
        Map<String, Object> attStmt = new LinkedHashMap<>();
        attStmt.put("alg", -7);
        attStmt.put("sig", signature);

        Map<String, Object> attestationObject = new LinkedHashMap<>();
        attestationObject.put("fmt", "packed");
        attestationObject.put("authData", authData);
        attestationObject.put("attStmt", attStmt);

        byte[] attestationObjectBytes = cborMapper.writeValueAsBytes(attestationObject);

        // --- 6. Assemble the final JSON response ---
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("clientDataJSON", Base64.getUrlEncoder().withoutPadding().encodeToString(clientDataJSONBytes));
        response.put("attestationObject", Base64.getUrlEncoder().withoutPadding().encodeToString(attestationObjectBytes));
        response.put("transports", List.of("internal"));

        Map<String, Object> finalJson = new LinkedHashMap<>();
        finalJson.put("id", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        finalJson.put("rawId", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        finalJson.put("type", "public-key");
        finalJson.put("response", response);
        finalJson.put("clientExtensionResults", Collections.emptyMap());

        return jsonMapper.writeValueAsString(finalJson);
    }

    /**
     * Generates a complete, cryptographically valid AuthenticationResponseJSON for testing purposes.
     * This simulates a client authenticating with an existing passkey.
     *
     * @param challenge    The challenge bytes provided by the server's getPasskeyRequestOptions.
     * @param rpId         The Relying Party ID for which the credential was created.
     * @param credentialId The ID of the credential to use for authentication.
     * @param keyPair      The key pair associated with the credentialId. The private key is required for signing.
     *
     * @return A JSON string representing the full response from the client.
     * @throws Exception if any cryptographic or serialization error occurs.
     */
    public static String generateValidAuthenticationResponseJSON(byte[] challenge, String rpId, byte[] credentialId, KeyPair keyPair, long counter) throws Exception {
        // --- 1. Define Constants ---
        String origin = "https://bankapp.online";

        // --- 2. Construct clientDataJSON ---
        Map<String, Object> clientData = new LinkedHashMap<>();
        clientData.put("type", "webauthn.get");
        clientData.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge));
        clientData.put("origin", origin);
        clientData.put("crossOrigin", false);

        byte[] clientDataJSONBytes = jsonMapper.writeValueAsBytes(clientData);
        byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJSONBytes);

        // --- 3. Construct Authenticator Data (authData) ---
        byte[] rpIdHash = MessageDigest.getInstance("SHA-256").digest(rpId.getBytes());
        byte flags = 0b00000101; // UP and UV flags set

        byte[] signCount = ByteArrayUtil.intToBytes((int) counter);

        ByteArrayOutputStream authDataStream = new ByteArrayOutputStream();
        authDataStream.write(rpIdHash);
        authDataStream.write(flags);
        authDataStream.write(signCount);
        byte[] authData = authDataStream.toByteArray();

        // --- 4. Create Signature ---
        ByteArrayOutputStream signatureBaseStream = new ByteArrayOutputStream();
        signatureBaseStream.write(authData);
        signatureBaseStream.write(clientDataHash);
        byte[] signatureBase = signatureBaseStream.toByteArray();

        Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA");
        ecdsaSignature.initSign(keyPair.getPrivate());
        ecdsaSignature.update(signatureBase);
        byte[] signature = ecdsaSignature.sign();

        // --- 5. Assemble the AuthenticatorAssertionResponse ---
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("clientDataJSON", Base64.getUrlEncoder().withoutPadding().encodeToString(clientDataJSONBytes));
        response.put("authenticatorData", Base64.getUrlEncoder().withoutPadding().encodeToString(authData));
        response.put("signature", Base64.getUrlEncoder().withoutPadding().encodeToString(signature));
        response.put("userHandle", null);

        // --- 6. Assemble the final JSON response ---
        Map<String, Object> finalJson = new LinkedHashMap<>();
        finalJson.put("id", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        finalJson.put("rawId", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        finalJson.put("type", "public-key");
        finalJson.put("response", response);
        finalJson.put("clientExtensionResults", Collections.emptyMap());

        return jsonMapper.writeValueAsString(finalJson);
    }
}
