package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.service.PasskeyOptionsPort;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;

import java.util.List;

public class StubPasskeyOptionsService implements PasskeyOptionsPort {
    @Override
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Session session) {
        return new PublicKeyCredentialCreationOptions(
                null,
                new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(
                        ByteArrayUtil.uuidToBytes(user.getId()),
                       user.getEmail().getValue(),
                       user.getEmail().getValue()
                ),
                session.challenge().challenge(),
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
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(List<Passkey> userCredentials, Session session) {
        return null;
    }

    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Session session) {
        return getPasskeyRequestOptions(null, session);
    }
}
