package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

public interface WebAuthnPort {

    CredentialRecord confirmRegistrationChallenge(String registrationResponseJSON, AuthSession sessionData);

    Object confirmAuthenticationChallenge(String authenticationResponseJSON, AuthSession sessionData);
}
