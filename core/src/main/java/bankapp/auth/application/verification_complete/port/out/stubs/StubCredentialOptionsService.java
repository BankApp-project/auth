package bankapp.auth.application.verification_complete.port.out.stubs;

import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.service.ByteArrayUtil;

import java.util.List;

public class StubCredentialOptionsService implements CredentialOptionsPort {
    @Override
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, byte[] challenge) {
        return new PublicKeyCredentialCreationOptions(
                null,
                new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(
                        ByteArrayUtil.uuidToBytes(user.getId()),
                       user.getEmail().getValue(),
                       user.getEmail().getValue()
                ),
                challenge,
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
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(List<Passkey> userCredentials, byte[] challenge) {
        return null;
    }
}
