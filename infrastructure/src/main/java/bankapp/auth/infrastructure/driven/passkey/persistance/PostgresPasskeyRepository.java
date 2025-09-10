package bankapp.auth.infrastructure.driven.passkey.persistance;

import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.persistance.converters.JpaToEntityPasskeyMapper;
import bankapp.auth.infrastructure.driven.passkey.persistance.dto.JpaPasskey;
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
    public Optional<Passkey> load(UUID credentialId) {
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
    public void save(PasskeyRegistrationData passkeyRegistrationData) throws CredentialAlreadyExistsException {
        var jpaPasskey = mapToJpaPasskey(passkeyRegistrationData);

        jpaPasskeyRepository.save(jpaPasskey);
    }

    private JpaPasskey mapToJpaPasskey(PasskeyRegistrationData passkeyRegistrationData) {
        return new JpaPasskey(
                passkeyRegistrationData.id(),
                passkeyRegistrationData.userHandle(),
                passkeyRegistrationData.type(),
                passkeyRegistrationData.publicKey(),
                passkeyRegistrationData.signCount(),
                passkeyRegistrationData.uvInitialized(),
                passkeyRegistrationData.backupEligible(),
                passkeyRegistrationData.backupState(),
                passkeyRegistrationData.transports(),
                passkeyRegistrationData.extensions(),
                passkeyRegistrationData.attestationObject(),
                passkeyRegistrationData.attestationClientDataJSON()
        );
    }

    /// This method updates signCount
    @Override
    public void updateSignCount(Passkey updatedCredential) {
        jpaPasskeyRepository.updateSignCount(updatedCredential.getId(), updatedCredential.getSignCount());
    }
}
