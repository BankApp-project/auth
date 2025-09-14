package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebAuthnVerificationService implements WebAuthnVerificationPort {

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    private final AuthenticationParametersProvider authenticationParametersProvider;
    private final RegistrationParametersProvider registrationParametersProvider;
    private final RegistrationDataMapper registrationDataMapper;

    @Override
    public Passkey confirmRegistrationChallenge(String registrationResponseJSON, Session sessionData) {
        try {
            var registrationParameters = getRegistrationParameters(sessionData);

            var registrationData = getRegistrationData(registrationResponseJSON, registrationParameters);

            return registrationDataMapper.toDomainEntity(registrationData, sessionData.userId());
        } catch (RuntimeException e) {
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
        }
    }

    private RegistrationParameters getRegistrationParameters(Session sessionData) {
        return registrationParametersProvider.getRegistrationParameters(sessionData);
    }

    private RegistrationData getRegistrationData(String registrationResponseJSON, RegistrationParameters registrationParameters) {
        return webAuthnManager.verifyRegistrationResponseJSON(registrationResponseJSON, registrationParameters);
    }


    @Override
    public Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Session sessionData, Passkey passkey) {

        var authParams = getAuthenticationParameters(sessionData, passkey);

        webAuthnManager.verifyAuthenticationResponseJSON(authenticationResponseJSON, authParams);
        passkey.signCountIncrement();
        return passkey;
    }

    private AuthenticationParameters getAuthenticationParameters(Session sessionData, Passkey passkey) {
        return authenticationParametersProvider.getAuthenticationParameters(sessionData, passkey);
    }


}
