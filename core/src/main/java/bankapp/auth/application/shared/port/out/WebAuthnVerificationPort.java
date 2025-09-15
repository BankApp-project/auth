package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;

public interface WebAuthnVerificationPort {

    Passkey confirmRegistrationChallenge(String registrationResponseJSON, Session sessionData);

    /**
     *
     * @return updated `Passkey` with `signCount`++
     */
    Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Session sessionData, Passkey passkey) throws MaliciousCounterException;
}
