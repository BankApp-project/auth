package bankapp.auth.infrastructure.persistance.passkey;

import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.persistance.passkey.converters.JpaToEntityPasskeyMapper;
import bankapp.auth.infrastructure.persistance.passkey.dto.JpaPasskey;
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
        var jpaPasskeyOptional = jpaPasskeyRepository.findById(credentialId);

        if (jpaPasskeyOptional.isEmpty()) {
            return Optional.empty();
        }

        var jpaPasskey = jpaPasskeyOptional.get();
        var passkey = mapper.toDomainPasskey(jpaPasskey);

        return Optional.of(passkey);
    }

    // user handle
    @Override
    public List<Passkey> loadForUserId(UUID userId) {
        return List.of();
    }

    @Override
    public void save(PasskeyRegistrationData passkeyRegistrationData) throws CredentialAlreadyExistsException {
        var jpaPasskey = new JpaPasskey(
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

        jpaPasskeyRepository.save(jpaPasskey);
    }

    /// This method updates signCount
    @Override
    public void update(Passkey updatedCredential) {

    }
}
