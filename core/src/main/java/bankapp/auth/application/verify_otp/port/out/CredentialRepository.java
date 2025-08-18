package bankapp.auth.application.verify_otp.port.out;

import bankapp.auth.domain.model.CredentialRecord;

import java.util.List;
import java.util.UUID;

public interface CredentialRepository {

    /**
     * Loads credential records for the given ID.
     * @param id the UUID to search for
     * @return a list of credential records, may be empty if no records found
     */
    List<CredentialRecord> load(UUID id);
}
