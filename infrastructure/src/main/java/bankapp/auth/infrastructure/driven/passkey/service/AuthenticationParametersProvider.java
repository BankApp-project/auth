package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyConfiguration;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthenticationParametersProvider {

    private final PasskeyConfiguration passkeyConfig;
    private final PasskeyToCredentialRecordMapper passkeyToCredentialRecordMapper;

    public AuthenticationParameters getAuthenticationParameters(@NotNull Session sessionData, @NotNull Passkey passkey) {
        return new AuthenticationParameters(
                getServerProperty(sessionData),
                getCredentialRecord(passkey),
                getAllowedCredentials(sessionData.credentialsIds()),
                passkeyConfig.userVerificationRequired(),
                passkeyConfig.userPresenceRequired()
        );
    }

    private ServerProperty getServerProperty(Session sessionData) {
        var challenge = new DefaultChallenge(sessionData.challenge().challenge());
        return new ServerProperty(passkeyConfig.origin(), passkeyConfig.rpId(), challenge);
    }

    private CredentialRecord getCredentialRecord(Passkey source) {
        return passkeyToCredentialRecordMapper.from(source);
    }

    private List<byte[]> getAllowedCredentials(Optional<List<UUID>> uuids) {

        if (uuids.isEmpty()) {
            return null;
        }

        return uuids.get().stream()
                .map(ByteArrayUtil::uuidToBytes)
                .toList();
    }
}
