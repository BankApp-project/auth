package bankapp.auth.application.shared.port.out.persistance;

import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

import java.util.List;
import java.util.UUID;

public interface CredentialRepository {

    CredentialRecord load(byte[] credentialId);

    /**
     * Loads credential records for the given ID.
     * @param userId the User ID to search for
     * @return a list of credential records, may be empty if no records found
     */
    List<CredentialRecord> loadForUserId(UUID userId);

    void save(CredentialRecord credentialRecord) throws CredentialAlreadyExistsException;
}