package bankapp.auth.application.shared.port.out.persistance;

import bankapp.auth.application.shared.exception.CredentialAlreadyExistsException;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

import java.util.List;
import java.util.UUID;

public interface CredentialRepository {

    /**
     * Loads credential records for the given ID.
     * @param id the UUID to search for
     * @return a list of credential records, may be empty if no records found
     */
    List<CredentialRecord> load(UUID id);

    void save(CredentialRecord credentialRecord) throws CredentialAlreadyExistsException;
}