package bankapp.auth.application.shared.port.out.dto;

import bankapp.auth.application.shared.enums.UserVerificationRequirement;

import java.util.List;
import java.util.Map;

/**
 * A library-agnostic representation of the PublicKeyCredentialCreationOptions dictionary
 * from the W3C WebAuthn specification. This DTO is designed to be serialized to JSON
 * and sent to a client to initiate a navigator.credentials.create() call.
 * <p>
 * All field names and structures directly correspond to the official specification.
 *
 * @see <a href="https://www.w3.org/TR/webauthn-3/#dictionary-makecredentialoptions">W3C WebAuthn Level 3: PublicKeyCredentialCreationOptions</a>
 */
public record PublicKeyCredentialCreationOptions(
        // NOTE: For JSON serialization, any field of type byte[] (like 'challenge'
        // and 'user.id') MUST be Base64URL encoded.

        /*
         * Information about the Relying Party.
         * (Required)
         */
        PublicKeyCredentialRpEntity rp,

        /*
         * Information about the user account.
         * (Required)
         */
        PublicKeyCredentialUserEntity user,

        /*
         * A cryptographically random buffer to prevent replay attacks.
         * (Required)
         */
        byte[] challenge,

        /*
         * A list of desired credential properties (type and algorithm).
         * (Required)
         */
        List<PublicKeyCredentialParameters> pubKeyCredParams,

        /*
         * The time, in milliseconds, that the user has to complete the ceremony.
         * (Optional)
         */
        Long timeout,

        /*
         * A list of existing credentials to prevent duplicate registrations on this device.
         * (Optional, defaults to empty list)
         */
        List<PublicKeyCredentialDescriptor> excludeCredentials,

        /*
         * Specifies requirements for the authenticator.
         * (Optional)
         */
        AuthenticatorSelectionCriteria authenticatorSelection,

        /*
         * A list of public key credential types, ordered from most to least preferred.
         * (Optional, defaults to empty list)
         * @see <a href="https://www.w3.org/TR/webauthn-3/#dom-publickeycredentialcreationoptions-hints">hints member</a>
         */
        List<String> hints,

        /*
         * The server's preference for attestation conveyance.
         * (Optional, defaults to "none")
         */
        String attestation,

        /*
         * A list of attestation statement format identifiers that the Relying Party is
         * willing to accept.
         * (Optional, defaults to empty list)
         * @see <a href="https://www.w3.org/TR/webauthn-3/#dom-publickeycredentialcreationoptions-attestationformats">attestationFormats member</a>
         */
        List<String> attestationFormats,

        /*
         * Client-side extensions to be used during the creation ceremony.
         * (Optional)
         */
        Map<String, Object> extensions
) {

    /**
     * Represents the Relying Party (server).
     * @see <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialrpentity">W3C: PublicKeyCredentialRpEntity</a>
     */
    public record PublicKeyCredentialRpEntity(String id, String name) {}

    /**
     * Represents the user account being registered.
     * @see <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialuserentity">W3C: PublicKeyCredentialUserEntity</a>
     */
    public record PublicKeyCredentialUserEntity(byte[] id, String name, String displayName) {}

    /**
     * Defines an acceptable combination of credential type and cryptographic algorithm.
     * @see <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialparameters">W3C: PublicKeyCredentialParameters</a>
     */
    public record PublicKeyCredentialParameters(String type, long alg) {}

    /**
     * Specifies the Relying Party's requirements for the desired authenticator.
     * @see <a href="https://www.w3.org/TR/webauthn-3/#dictdef-authenticatorselectioncriteria">W3C: AuthenticatorSelectionCriteria</a>
     */
    public record AuthenticatorSelectionCriteria(
            String authenticatorAttachment,
            boolean requireResidentKey,
            UserVerificationRequirement userVerification
    ) {}
}