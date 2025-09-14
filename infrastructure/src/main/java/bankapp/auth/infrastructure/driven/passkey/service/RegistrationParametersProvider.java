package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyConfiguration;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationParametersProvider {

    private final PasskeyConfiguration passkeyConfig;

    public RegistrationParameters getRegistrationParameters(Session sessionData) {
        var serverProperty = getServerProperty(sessionData);

        return createRegistrationParameters(serverProperty);
    }

    private ServerProperty getServerProperty(Session sessionData) {
        var challenge = new DefaultChallenge(sessionData.value());
        return new ServerProperty(passkeyConfig.origin(), passkeyConfig.rpId(), challenge);
    }

    private RegistrationParameters createRegistrationParameters(ServerProperty serverProperty) {
        return new RegistrationParameters(serverProperty,
                PublicKeyCredentialParametersProvider.getInfraPubKeyCredParams(),
                passkeyConfig.userVerificationRequired(),
                passkeyConfig.userPresenceRequired());
    }

}
