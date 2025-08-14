package bankapp.auth.domain.model.dto;

import java.util.List;
import java.util.Map;

/**
 * A library-agnostic representation of the PublicKeyCredentialRequestOptions dictionary
 * from the W3C WebAuthn specification. This DTO is designed to be serialized to JSON
 * and sent to a client to initiate a navigator.credentials.get() call for authentication.
 * <p>
 * All field names and structures directly correspond to the official specification.
 *
 * @see <a href="https://www.w3.org/TR/webauthn-3/#dictionary-assertion-options">W3C WebAuthn Level 3: PublicKeyCredentialRequestOptions</a>
 */
public record PublicKeyCredentialRequestOptions(
        // NOTE: For JSON serialization, any field of type byte[] (like 'challenge')
        // MUST be Base64URL encoded.

        /**
         * A cryptographically random buffer sent from the Relying Party to prevent
         * replay attacks.
         * (Required)
         */
        byte[] challenge,

        /**
         * The time, in milliseconds, that the user has to complete the authentication
         * ceremony.
         * (Optional)
         */
        Long timeout,

        /**
         * The unique identifier (e.g., "localhost" or "login.example.com") of the
         * Relying Party, which must match the origin where the credential was created.
         * (Optional)
         */
        String rpId,

        /**
         * A list of acceptable credentials for this authentication request. The client
         * will prompt the user to select from authenticators that can produce an
         * assertion for one of these credentials.
         * (Optional)
         */
        List<PublicKeyCredentialDescriptor> allowCredentials,

        /**
         * The Relying Party's requirement for user verification (e.g., PIN or biometrics)
         * for this authentication ceremony.
         * (Optional, defaults to "preferred")
         */
        String userVerification,

        /**
         * Client-side extensions to be used during the authentication ceremony.
         * (Optional)
         */
        Map<String, Object> extensions
) {

    /**
     * Describes an existing public key credential that is acceptable for authentication.
     * This record is reused from the creation options.
     *
     * @param type The credential type. Must be "public-key".
     * @param id The Base64URL-encoded credential ID of the credential.
     * @param transports A list of transport methods (e.g., "internal", "usb", "nfc")
     *                   that may be used to transport the credential. This is an optional hint.
     *
     * @see <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialdescriptor">W3C: PublicKeyCredentialDescriptor</a>
     */
    public record PublicKeyCredentialDescriptor(String type, byte[] id, List<String> transports) {}
}