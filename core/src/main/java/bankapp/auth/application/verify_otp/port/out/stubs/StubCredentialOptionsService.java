package bankapp.auth.application.verify_otp.port.out.stubs;

import bankapp.auth.application.verify_otp.port.out.CredentialOptionsPort;
import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.domain.service.ByteArrayUtil;

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
