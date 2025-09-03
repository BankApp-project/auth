package bankapp.auth.application.shared.port.out.persistance;

import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.domain.model.Passkey;

import java.util.List;
import java.util.UUID;

public interface CredentialRepository {

    Passkey load(byte[] credentialId);

    /**
     * Loads credential records for the given ID.
     * @param userId the User ID to search for
     * @return a list of credential records, may be empty if no records found
     */
    List<Passkey> loadForUserId(UUID userId);

    void save(PasskeyRegistrationData passkeyRegistrationData) throws CredentialAlreadyExistsException;

    void update(Passkey updatedCredential);
}