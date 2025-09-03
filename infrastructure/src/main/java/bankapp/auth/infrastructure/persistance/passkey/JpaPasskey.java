package bankapp.auth.infrastructure.persistance.passkey;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.infrastructure.persistance.passkey.converters.JsonToMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

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
@Entity
public class JpaPasskey {

    // === Core Fields Required for Authentication ===

    /*
     * The credential ID of the public key credential. This is a globally unique
     * identifier for the credential, used to look it up during authentication.
     * Stored as the raw byte array.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private byte[] id;

    /*
     * Corresponding User ID
     */
    @Column(name = "user_handle", nullable = false, updatable = false, unique = true)
    private UUID userHandle;

    /*
     * The credential type. For WebAuthn, this MUST be the string "public-key".
     */
    @Column(name = "type", nullable = false)
    private String type;

    /*
     * The COSE-formatted public key of the credential. The Relying Party uses
     * this key to verify authentication signatures from the user's authenticator.
     */
    @Column(name = "public_key", nullable = false, updatable = false, unique = true)
    private byte[] publicKey;

    /*
     * The signature counter of the credential. The Relying Party MUST store
     * this value and verify that it increases with each new authentication to
     * help detect cloned authenticators.
     */
    @Column(name = "sign_count", nullable = false)
    private long signCount;

    /*
     * A flag indicating that the user has been successfully verified (e.g., via PIN
     * or biometrics) for this credential at least once.
     */
    @Column(name = "uv_initialized", nullable = false)
    private boolean uvInitialized;

    // === Optional Flags and Metadata ===

    /*
     * A flag indicating if the authenticator reported that the credential is
     * "backup eligible".
     */
    @Column(name = "backup_eligible", nullable = false)
    private boolean backupEligible;

    /*
     * A flag indicating if the authenticator reported that the credential is
     * currently "backed up".
     */
    @Column(name = "backup_state", nullable = false)
    private boolean backupState;

    /*
     * A list of authenticator transport methods (e.g., "internal", "usb", "nfc")
     * that the client believes can be used to exercise the credential. May be empty.
     */
    // persist as string
    @Column(name = "transports")
    private List<AuthenticatorTransport> transports;

    /*
     * The client extension outputs created by the authenticator for this
     * credential during the original registration ceremony. Can be null.
     */
    @Convert(converter = JsonToMapConverter.class)
    @Column(name = "client_extensions", columnDefinition = "JSONB")
    private Map<String, Object> extensions;

    // === Attestation Data for Auditing and Verification ===

    /*
     * The raw `attestationObject` received from the authenticator during registration.
     * This is a CBOR-encoded byte array. Storing this allows the Relying Party to
     * inspect the credential's attestation statement at any time for auditing,
     * such as to verify the authenticator's model or certification level.
     */
    @Column(name = "attestation", nullable = false)
    private byte[] attestationObject;

    /*
     * The raw `clientDataJSON` received during registration. This is a UTF-8 encoded
     * byte array. Storing this, along with the attestationObject, allows the
     * Relying Party to re-verify the original attestation signature at a later date.
     */
    @Column(name = "attestation_client_data")
    private byte[] attestationClientDataJSON;

    /**
     * Constructor to initialize all fields.
     */
    public JpaPasskey(
            byte[] id,
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
        // Validate required fields
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("Credential ID cannot be null or empty");
        }
        if (userHandle == null) {
            throw new IllegalArgumentException("User handle cannot be null");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (publicKey == null || publicKey.length == 0) {
            throw new IllegalArgumentException("Public key cannot be null or empty");
        }
        this.id = id;
        this.userHandle = userHandle;
        this.type = type;
        this.publicKey = publicKey;
        this.signCount = signCount;
        this.uvInitialized = uvInitialized;
        this.backupEligible = backupEligible;
        this.backupState = backupState;
        this.transports = transports;
        this.extensions = extensions;
        this.attestationObject = attestationObject;
        this.attestationClientDataJSON = attestationClientDataJSON;
    }

    protected JpaPasskey() {
        // for JPA
    }

    public JpaPasskey signCountIncrement() {
        this.signCount++;
        return this;
    }

    public JpaPasskey signCountUpdate(long signCount) {
        this.signCount = signCount;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        JpaPasskey that = (JpaPasskey) o;
        return getId() != null && Arrays.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}