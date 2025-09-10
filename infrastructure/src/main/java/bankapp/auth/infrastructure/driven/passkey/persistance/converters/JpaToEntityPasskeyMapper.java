package bankapp.auth.infrastructure.driven.passkey.persistance.converters;

import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.persistance.dto.JpaPasskey;
import org.springframework.stereotype.Component;

/**
 * Maps between the persistence-layer {@link JpaPasskey} entity and the
 * domain-layer {@link Passkey} model.
 */
@Component
public class JpaToEntityPasskeyMapper {

    /**
     * Maps a {@link JpaPasskey} entity from the database to a {@link Passkey} domain model.
     * The domain model is a subset of the JPA entity, containing the fields necessary for
     * authentication ceremonies.
     *
     * @param jpaPasskey The JPA entity to map from.
     * @return A new {@link Passkey} domain object, or null if the input is null.
     */
    public Passkey toDomainPasskey(JpaPasskey jpaPasskey) {
        if (jpaPasskey == null) {
            return null;
        }

        return new Passkey(
                jpaPasskey.getId(),
                jpaPasskey.getUserHandle(),
                jpaPasskey.getPublicKey(),
                jpaPasskey.getSignCount(),
                jpaPasskey.isUvInitialized(),
                jpaPasskey.isBackupState(),
                jpaPasskey.getTransports()
        );
    }

    /**
     * Updates an existing {@link JpaPasskey} entity with mutable data from a {@link Passkey} domain model.
     * <p>
     * This method is used to persist changes made in the domain layer back to the database,
     * for example, updating the signature counter after a successful authentication.
     * <p>
     * Note: A full {@code toJpaPasskey(Passkey passkey)} method is not provided because the
     * {@link Passkey} domain object does not contain all the required data (e.g., attestation data)
     * to construct a new {@link JpaPasskey} entity. New entities should be created during the
     * registration process where all data is available.
     *
     * @param jpaPasskey The persistence entity to be updated.
     * @param passkey    The domain model containing the updated information.
     * @return The updated {@link JpaPasskey} entity.
     */
    public JpaPasskey updateJpaPasskey(JpaPasskey jpaPasskey, Passkey passkey) {
        if (jpaPasskey == null || passkey == null) {
            return jpaPasskey;
        }

        // The only mutable field in the Passkey domain object is signCount.
        // Update the JpaPasskey's sign count to match.
        jpaPasskey.signCountUpdate(passkey.getSignCount());

        return jpaPasskey;
    }
}