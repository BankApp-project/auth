package bankapp.auth.infrastructure.persistance.passkey.converters;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.persistance.passkey.dto.JpaPasskey;
import com.github.f4b6a3.uuid.alt.GUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class JpaToEntityPasskeyMapperTest {

    private JpaToEntityPasskeyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JpaToEntityPasskeyMapper();
    }

    private JpaPasskey createSampleJpaPasskey() {
        var credentialId = GUID.v7().toUUID();
        var userId = GUID.v4().toUUID();
        return new JpaPasskey(
                userId,
                UUID.randomUUID(),
                "public-key",
                "public-key-bytes".getBytes(),
                100L,
                true,
                true,
                true,
                Arrays.asList(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.USB),
                Collections.singletonMap("extKey", "extValue"),
                "attestation-object-bytes".getBytes(),
                "client-data-json-bytes".getBytes()
        );
    }

    @Test
    void toDomainPasskey_shouldMapAllFieldsCorrectly() {
        // Arrange
        JpaPasskey jpaPasskey = createSampleJpaPasskey();

        // Act
        Passkey domainPasskey = mapper.toDomainPasskey(jpaPasskey);

        // Assert
        assertNotNull(domainPasskey);
        assertEquals(jpaPasskey.getId(), domainPasskey.getId());
        assertEquals(jpaPasskey.getUserHandle(), domainPasskey.getUserHandle());
        assertArrayEquals(jpaPasskey.getPublicKey(), domainPasskey.getPublicKey());
        assertEquals(jpaPasskey.getSignCount(), domainPasskey.getSignCount());
        assertEquals(jpaPasskey.isUvInitialized(), domainPasskey.isUvInitialized());
        assertEquals(jpaPasskey.isBackupState(), domainPasskey.isBackupState());
        assertEquals(jpaPasskey.getTransports(), domainPasskey.getTransports());
    }

    @Test
    void toDomainPasskey_shouldReturnNull_whenInputIsNull() {
        // Act & Assert
        assertNull(mapper.toDomainPasskey(null));
    }

    @Test
    void updateJpaPasskey_shouldUpdateMutableFields() {
        // Arrange
        JpaPasskey originalJpaPasskey = createSampleJpaPasskey();
        long originalSignCount = originalJpaPasskey.getSignCount();

        // Create a domain object from the Jpa object, then modify it
        Passkey domainPasskey = mapper.toDomainPasskey(originalJpaPasskey);
        domainPasskey.signCountIncrement(); // new sign count is 101

        // Act
        mapper.updateJpaPasskey(originalJpaPasskey, domainPasskey);

        // Assert
        // Check that the sign count was updated correctly
        assertEquals(originalSignCount + 1, originalJpaPasskey.getSignCount());
        assertEquals(domainPasskey.getSignCount(), originalJpaPasskey.getSignCount());

        // Check that other fields (which are immutable in the domain object) remained unchanged
        assertEquals(originalJpaPasskey.getId(),domainPasskey.getId());
    }
}
