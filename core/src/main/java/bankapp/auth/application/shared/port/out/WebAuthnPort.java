package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.port.out.dto.RegistrationSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

public interface WebAuthnPort {

    CredentialRecord confirmRegistrationChallenge(String registrationResponseJSON, RegistrationSession sessionData);

    /**
     *
     * @return updated `CredentialRecord` with `signCount`++
     */
    CredentialRecord confirmAuthenticationChallenge(String authenticationResponseJSON, RegistrationSession sessionData, CredentialRecord credentialRecord);
}
