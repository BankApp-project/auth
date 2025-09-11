package bankapp.auth.application.shared.port.out;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.domain.model.Passkey;

public interface WebAuthnVerificationPort {

    PasskeyRegistrationData confirmRegistrationChallenge(String registrationResponseJSON, Challenge challengeData);

    /**
     *
     * @return updated `Passkey` with `signCount`++
     */
    //todo I need to know what fields from Passkey is used for this method and fetch only those. using dto record or interface with JPA repo
    Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Challenge challengeData, Passkey passkey);
}
