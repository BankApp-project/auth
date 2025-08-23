package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;

public interface WebAuthnPort {

    Passkey confirmRegistrationChallenge(String registrationResponseJSON, Challenge sessionData);

    /**
     *
     * @return updated `Passkey` with `signCount`++
     */
    Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Challenge sessionData, Passkey passkey);
}
