package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyConfiguration;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationParametersProvider {

    private final PasskeyConfiguration passkeyConfig;
    private final PasskeyToCredentialRecordMapper passkeyToCredentialRecordMapper;

    public AuthenticationParameters getAuthenticationParameters(Challenge challengeData, Passkey passkey) {
        return new AuthenticationParameters(
                getServerProperty(challengeData),
                getCredentialRecord(passkey),
                getAllowedCredentials(),
                passkeyConfig.userVerificationRequired(),
                passkeyConfig.userPresenceRequired());
    }

    private ServerProperty getServerProperty(Challenge challengeData) {
        var challenge = new DefaultChallenge(challengeData.value());
        return new ServerProperty(passkeyConfig.origin(), passkeyConfig.rpId(), challenge);
    }

    private CredentialRecord getCredentialRecord(Passkey source) {
        return passkeyToCredentialRecordMapper.from(source);
    }

    private @Nullable List<byte[]> getAllowedCredentials() {
        throw new UnsupportedOperationException(); //not implemented yet!
    }
}
