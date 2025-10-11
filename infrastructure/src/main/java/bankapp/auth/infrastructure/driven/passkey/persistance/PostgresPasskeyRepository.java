package bankapp.auth.infrastructure.driven.passkey.persistance;

import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.persistance.converters.JpaToEntityPasskeyMapper;
import bankapp.auth.infrastructure.driven.passkey.persistance.jpa.JpaPasskey;
import bankapp.auth.infrastructure.driven.passkey.persistance.jpa.JpaPasskeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostgresPasskeyRepository implements PasskeyRepository {

    private final JpaPasskeyRepository jpaPasskeyRepository;
    private final JpaToEntityPasskeyMapper mapper;

    @Override
    public Optional<Passkey> load(byte[] credentialId) {
        // we should not fetch all the data from the repo. Only the needed one.
        var jpaPasskeyOptional = jpaPasskeyRepository.findById(credentialId);

        if (jpaPasskeyOptional.isEmpty()) {
            return Optional.empty();
        }

        var jpaPasskey = jpaPasskeyOptional.get();
        var passkey = mapper.toDomainPasskey(jpaPasskey);

        return Optional.of(passkey);
    }

    @Override
    public List<Passkey> loadForUserId(UUID userId) {
        var res = jpaPasskeyRepository.findAllByUserHandle(userId);

        return res.stream()
                .map(mapper::toDomainPasskey)
                .toList();
    }

    @Override
    public void save(Passkey passkey) throws CredentialAlreadyExistsException {
        var jpaPasskey = mapToJpaPasskey(passkey);

        jpaPasskeyRepository.save(jpaPasskey);
    }

    private JpaPasskey mapToJpaPasskey(Passkey passkey) {

        return new JpaPasskey(
                passkey.getId(),
                passkey.getUserHandle(),
                passkey.getType(),
                passkey.getPublicKey(),
                passkey.getSignCount(),
                passkey.isUvInitialized(),
                passkey.isBackupEligible(),
                passkey.isBackupState(),
                passkey.getTransports(),
                passkey.getExtensions(),
                passkey.getAttestationObject(),
                passkey.getAttestationClientDataJSON()
        );
    }

    /// This method updates signCount
    @Override
    public void updateSignCount(Passkey updatedCredential) {
        // Convert byte[] id to UUID for JPA query
        UUID idAsUuid = ByteArrayUtil.bytesToUuid(updatedCredential.getId());
        jpaPasskeyRepository.updateSignCount(idAsUuid, updatedCredential.getSignCount());
    }
}
