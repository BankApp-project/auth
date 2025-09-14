package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Challenge;
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

    public RegistrationParameters getRegistrationParameters(Challenge challengeData) {
        var serverProperty = getServerProperty(challengeData);

        return createRegistrationParameters(serverProperty);
    }

    private ServerProperty getServerProperty(Challenge challengeData) {
        var challenge = new DefaultChallenge(challengeData.value());
        return new ServerProperty(passkeyConfig.origin(), passkeyConfig.rpId(), challenge);
    }

    private RegistrationParameters createRegistrationParameters(ServerProperty serverProperty) {
        return new RegistrationParameters(serverProperty,
                PublicKeyCredentialParametersProvider.getInfraPubKeyCredParams(),
                passkeyConfig.userVerificationRequired(),
                passkeyConfig.userPresenceRequired());
    }

}
