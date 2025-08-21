package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

public interface WebAuthnPort {

    CredentialRecord confirmRegistrationChallenge(String registrationResponseJSON, AuthSession sessionData);

    // there should also be included user public key i think, but not sure.
    Object confirmAuthenticationChallenge(String authenticationResponseJSON, AuthSession sessionData, CredentialRecord credentialRecord);
}
