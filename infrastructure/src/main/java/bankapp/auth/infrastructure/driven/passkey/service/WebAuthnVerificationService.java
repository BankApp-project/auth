package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.verifier.exception.MaliciousCounterValueException;
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

            var registrationData = verifyRegistrationResponse(registrationResponseJSON, registrationParameters);

            return mapToPasskey(sessionData, registrationData);
        } catch (RuntimeException e) {
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
        }
    }

    private RegistrationParameters getRegistrationParameters(Session sessionData) {
        return registrationParametersProvider.getRegistrationParameters(sessionData);
    }

    private RegistrationData verifyRegistrationResponse(String registrationResponseJSON, RegistrationParameters registrationParameters) {
        return webAuthnManager.verifyRegistrationResponseJSON(registrationResponseJSON, registrationParameters);
    }

    private UUID getUserId(Session sessionData) {
        return sessionData.userId().orElseThrow(
                () -> new RegistrationConfirmAttemptException("User ID is missing in session data")
        );
    }

    private Passkey mapToPasskey(Session sessionData, RegistrationData registrationData) {
        return registrationDataMapper.toDomainEntity(registrationData, getUserId(sessionData));
    }

    @Override
    public Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Session sessionData, Passkey passkey) throws MaliciousCounterException {
        try {

            var authParams = getAuthenticationParameters(sessionData, passkey);

            var authenticationData = verifyAuthenticationResponse(authenticationResponseJSON, authParams);

            setSignCount(passkey, authenticationData);

            return passkey;
        } catch (MaliciousCounterValueException e) {
            throw new MaliciousCounterException("Malicious counter value detected in authentication response.");
        }
    }

    private AuthenticationData verifyAuthenticationResponse(String authenticationResponseJSON, AuthenticationParameters authParams) {
        return webAuthnManager.verifyAuthenticationResponseJSON(authenticationResponseJSON, authParams);
    }

    private AuthenticationParameters getAuthenticationParameters(Session sessionData, Passkey passkey) {
        return authenticationParametersProvider.getAuthenticationParameters(sessionData, passkey);
    }

    private void setSignCount(Passkey passkey, AuthenticationData res) {
        Objects.requireNonNull(res.getAuthenticatorData());
        passkey.setSignCount(res.getAuthenticatorData().getSignCount());
    }


}
