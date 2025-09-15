package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.WebAuthnAuthenticationManager;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationConfirmationHandler {

    private final WebAuthnAuthenticationManager webAuthnManager = new WebAuthnAuthenticationManager();

    private final AuthenticationParametersProvider authenticationParametersProvider;

    public Passkey handle(String authenticationResponseJSON, Session sessionData, Passkey passkey) throws MaliciousCounterException {
        var authParams = getAuthenticationParameters(sessionData, passkey);

        var authenticationData = verifyAuthenticationResponse(authenticationResponseJSON, authParams);

        setSignCount(passkey, authenticationData);

        return passkey;
    }

    private AuthenticationParameters getAuthenticationParameters(Session sessionData, Passkey passkey) {
        return authenticationParametersProvider.getAuthenticationParameters(sessionData, passkey);
    }

    private AuthenticationData verifyAuthenticationResponse(String authenticationResponseJSON, AuthenticationParameters authParams) {
        return webAuthnManager.verify(authenticationResponseJSON, authParams);
    }

    private void setSignCount(Passkey passkey, AuthenticationData res) {
        Objects.requireNonNull(res.getAuthenticatorData());
        passkey.setSignCount(res.getAuthenticatorData().getSignCount());
    }
}
