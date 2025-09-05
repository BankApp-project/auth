package bankapp.auth.domain.model;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

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
     * A flag indicating if the authenticator reported that the credential is
     * currently "backed up".
     */
    private final boolean backupState;

    /*
     * A list of authenticator transport methods (e.g., "internal", "usb", "nfc")
     * that the client believes can be used to exercise the credential. May be empty.
     */
    private final List<AuthenticatorTransport> transports;


    /**
     * Constructor to initialize all fields.
     */
    public Passkey(
            UUID id,
            UUID userHandle,
            byte[] publicKey,
            long signCount,
            boolean uvInitialized,
            boolean backupState,
            List<AuthenticatorTransport> transports
    ) {
        this.id = id;
        this.userHandle = userHandle;
        this.publicKey = publicKey;
        this.signCount = signCount;
        this.uvInitialized = uvInitialized;
        this.backupState = backupState;
        this.transports = transports;
    }

    public Passkey signCountIncrement() {
        this.signCount++;
        return this;
    }
}