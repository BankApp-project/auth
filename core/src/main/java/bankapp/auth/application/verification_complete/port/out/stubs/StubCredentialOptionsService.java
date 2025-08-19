package bankapp.auth.application.verification_complete.port.out.stubs;

import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.service.ByteArrayUtil;

import java.util.List;

public class StubCredentialOptionsService implements CredentialOptionsPort {
    @Override
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, byte[] defaultChallenge) {
        return new PublicKeyCredentialCreationOptions(
                null,
                new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(
                        ByteArrayUtil.uuidToBytes(user.getId()),
                       user.getEmail().getValue(),
                       user.getEmail().getValue()
                ),
                defaultChallenge,
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
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(List<CredentialRecord> userCredentials, byte[] challenge) {
        return null;
    }
}
