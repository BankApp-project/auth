package bankapp.auth.application.verification.complete.port.out.stubs;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification.complete.port.out.CredentialOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.service.ByteArrayUtil;

import java.util.List;

public class StubCredentialOptionsService implements CredentialOptionsPort {
    @Override
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Challenge challenge) {
        return new PublicKeyCredentialCreationOptions(
                null,
                new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(
                        ByteArrayUtil.uuidToBytes(user.getId()),
                       user.getEmail().getValue(),
                       user.getEmail().getValue()
                ),
                challenge.value(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null

        );
    }

    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(List<Passkey> userCredentials, Challenge challenge) {
        return null;
    }

    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Challenge challenge) {
        return getPasskeyRequestOptions(null, challenge);
    }
}
