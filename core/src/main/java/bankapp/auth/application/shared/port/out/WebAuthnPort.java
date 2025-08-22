package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

public interface WebAuthnPort {

    CredentialRecord confirmRegistrationChallenge(String registrationResponseJSON, Challenge sessionData);

    /**
     *
     * @return updated `CredentialRecord` with `signCount`++
     */
    CredentialRecord confirmAuthenticationChallenge(String authenticationResponseJSON, Challenge sessionData, CredentialRecord credentialRecord);
}
