package bankapp.auth.domain.model;

import bankapp.auth.domain.model.enums.AuthenticatorTransport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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
@Getter
@EqualsAndHashCode
public class Passkey {

    // === Core Fields Required for Authentication ===

    /*
     * The credential ID of the public key credential. This is a globally unique
     * identifier for the credential, used to look it up during authentication.
     * Stored as the raw byte array.
     */
    private final UUID id;

    /*
     * Corresponding User ID
     */
    private final UUID userHandle;

    /*
     * The type of credential (typically "public-key" for WebAuthn).
     */
    private final String type;

    /*
     * The COSE-formatted public key of the credential. The Relying Party uses
     * this key to verify authentication signatures from the user's authenticator.
     */
    private final byte[] publicKey;

    /*
     * The signature counter of the credential. The Relying Party MUST store
     * this value and verify that it increases with each new authentication to
     * help detect cloned authenticators.
     * NOTE: This field is not final to allow for modification.
     */
    @Setter
    private long signCount;

    /*
     * A flag indicating that the user has been successfully verified (e.g., via PIN
     * or biometrics) for this credential at least once.
     */
    private final boolean uvInitialized;

    // === Optional Flags and Metadata ===

    /*
     * A flag indicating if the authenticator supports backup eligibility.
     */
    private final boolean backupEligible;

    /*
     * A flag indicating if the authenticator reported that the credential is
     * currently "backed up".
     */
    private final boolean backupState;

    /*
     * A list of authenticator transport methods (e.g., "internal", "usb", "nfc")
     * that the client believes can be used to exercise the credential. May be empty.
     */
    private final List<AuthenticatorTransport> transports;

    /*
     * WebAuthn extensions data.
     */
    private final Map<String, Object> extensions;

    // === Attestation Data ===

    /*
     * The attestation object from the original registration ceremony.
     */
    private final byte[] attestationObject;

    /*
     * The client data JSON from the original registration ceremony.
     */
    private final byte[] attestationClientDataJSON;


    /**
     * Constructor to initialize all fields.
     */
    public Passkey(
            UUID id,
            UUID userHandle,
            String type,
            byte[] publicKey,
            long signCount,
            boolean uvInitialized,
            boolean backupEligible,
            boolean backupState,
            List<AuthenticatorTransport> transports,
            Map<String, Object> extensions,
            byte[] attestationObject,
            byte[] attestationClientDataJSON
    ) {
        this.id = Objects.requireNonNull(id);

        this.userHandle = Objects.requireNonNull(userHandle, "User handle cannot be null");

        this.type = Objects.requireNonNull(type, "Type cannot be null");

        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");

        if (signCount < 0) {
            throw new IllegalArgumentException("Sign count cannot be negative: " + signCount);
        }
        this.signCount = signCount;

        this.uvInitialized = uvInitialized;

        this.backupEligible = backupEligible;

        this.backupState = backupState;

        this.transports = Objects.requireNonNullElse(transports, new ArrayList<>());

        this.extensions = Objects.requireNonNullElse(extensions, new HashMap<>());

        this.attestationObject = Objects.requireNonNull(attestationObject, "Attestation object cannot be null");

        this.attestationClientDataJSON = Objects.requireNonNull(attestationClientDataJSON, "Attestation client data JSON cannot be null");
    }

    public Passkey signCountIncrement() {
        this.signCount++;
        return this;
    }
}