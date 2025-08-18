package bankapp.auth.domain.model;

import bankapp.auth.domain.model.enums.AuthenticatorTransport;

import java.util.List;
import java.util.Map;

/**
 * A library-agnostic representation of a Credential Record, which is the data
 * structure held by a Relying Party (server) in its user account database for
 * each registered passkey.
 * <p>
 * This model includes the core credential data required for authentication as well
 * as the supplementary attestation data from the original registration ceremony,
 * which is crucial for auditing and policy enforcement.
 *
 * @see <a href="https://www.w3.org/TR/webauthn-3/#credential-record">W3C WebAuthn Level 3: Credential Record</a>
 */
public record CredentialRecord(
        // === Core Fields Required for Authentication ===

        /*
         * The credential ID of the public key credential. This is a globally unique
         * identifier for the credential, used to look it up during authentication.
         * Stored as the raw byte array.
         */
        byte[] id,

        /*
         * Corresponding User ID as a byte array
         */
        byte[] userHandle,

        /*
         * The credential type. For WebAuthn, this MUST be the string "public-key".
         */
        String type,

        /*
         * The COSE-formatted public key of the credential. The Relying Party uses
         * this key to verify authentication signatures from the user's authenticator.
         */
        byte[] publicKey,

        /*
         * The signature counter of the credential. The Relying Party MUST store
         * this value and verify that it increases with each new authentication to
         * help detect cloned authenticators.
         */
        long signCount,

        /*
         * A flag indicating that the user has been successfully verified (e.g., via PIN
         * or biometrics) for this credential at least once.
         */
        boolean uvInitialized,

        // === Optional Flags and Metadata ===

        /*
         * A flag indicating if the authenticator reported that the credential is
         * "backup eligible".
         */
        boolean backupEligible,

        /*
         * A flag indicating if the authenticator reported that the credential is
         * currently "backed up".
         */
        boolean backupState,

        /*
         * A list of authenticator transport methods (e.g., "internal", "usb", "nfc")
         * that the client believes can be used to exercise the credential. May be empty.
         */
        List<AuthenticatorTransport> transports,

        /*
         * The client extension outputs created by the authenticator for this
         * credential during the original registration ceremony. Can be null.
         */
        Map<String, Object> extensions,

        // === Attestation Data for Auditing and Verification ===

        /*
         * The raw `attestationObject` received from the authenticator during registration.
         * This is a CBOR-encoded byte array. Storing this allows the Relying Party to
         * inspect the credential's attestation statement at any time for auditing,
         * such as to verify the authenticator's model or certification level.
         */
        byte[] attestationObject,

        /*
         * The raw `clientDataJSON` received during registration. This is a UTF-8 encoded
         * byte array. Storing this, along with the attestationObject, allows the
         * Relying Party to re-verify the original attestation signature at a later date.
         */
        byte[] attestationClientDataJSON
) {}
