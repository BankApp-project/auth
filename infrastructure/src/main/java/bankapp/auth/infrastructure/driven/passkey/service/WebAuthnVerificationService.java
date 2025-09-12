package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyConfiguration;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebAuthnVerificationService implements WebAuthnVerificationPort {

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    private final PasskeyConfiguration passkeyConfig;
    private final RegistrationDataMapper registrationDataMapper;

    @Override
    public Passkey confirmRegistrationChallenge(String registrationResponseJSON, Challenge challengeData) {
        try {
            var registrationParameters = getRegistrationParameters(challengeData);

            var registrationData = webAuthnManager.verifyRegistrationResponseJSON(registrationResponseJSON, registrationParameters);

            return registrationDataMapper.toDomainEntity(registrationData, challengeData.userId());
        } catch (RuntimeException e) {
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
        }
    }

    private RegistrationParameters getRegistrationParameters(Challenge challengeData) {
        var serverProperty = getServerProperty(challengeData);

        return getRegistrationParameters(serverProperty);
    }

    private ServerProperty getServerProperty(Challenge challengeData) {
        var challenge = new DefaultChallenge(challengeData.value());
        return new ServerProperty(passkeyConfig.origin(), passkeyConfig.rpId(), challenge);
    }

    private RegistrationParameters getRegistrationParameters(ServerProperty serverProperty) {
        return new RegistrationParameters(serverProperty,
                PublicKeyCredentialParametersProvider.getInfraPubKeyCredParams(),
                passkeyConfig.userVerificationRequired(),
                passkeyConfig.userPresenceRequired());
    }


    @Override
    public Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Challenge challengeData, Passkey passkey) {
        //todo
        //i have to do credentialRecord impl
        var authParams = new AuthenticationParameters(
                getServerProperty(challengeData),
                getCredentialRecord(passkey),
                getAllowedCredentials(),
                passkeyConfig.userVerificationRequired(),
                passkeyConfig.userPresenceRequired());

        webAuthnManager.verifyAuthenticationResponseJSON(authenticationResponseJSON, authParams);
        passkey.signCountIncrement();
        return passkey;
    }

    private @Nullable List<byte[]> getAllowedCredentials() {
        throw new UnsupportedOperationException(); //not implemented yet!
    }

    private CredentialRecord getCredentialRecord(Passkey source) {
        return PasskeyToCredentialRecordMapper.from(source);
    }
}
