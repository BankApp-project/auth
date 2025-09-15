package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

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

            return registrationDataMapper.toDomainEntity(registrationData, getUserId(sessionData));
        } catch (RuntimeException e) {
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
        }
    }

    private UUID getUserId(Session sessionData) {
        return sessionData.userId().orElseThrow(
                () -> new RegistrationConfirmAttemptException("User ID is missing in session data")
        );
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

        var authenticationData = webAuthnManager.verifyAuthenticationResponseJSON(authenticationResponseJSON, authParams);

        setSignCount(passkey, authenticationData);

        return passkey;
    }

    private AuthenticationParameters getAuthenticationParameters(Session sessionData, Passkey passkey) {
        return authenticationParametersProvider.getAuthenticationParameters(sessionData, passkey);
    }

    private void setSignCount(Passkey passkey, AuthenticationData res) {
        Objects.requireNonNull(res.getAuthenticatorData());
        passkey.setSignCount(res.getAuthenticatorData().getSignCount());
    }


}
