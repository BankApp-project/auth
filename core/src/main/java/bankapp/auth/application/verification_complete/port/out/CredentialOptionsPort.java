package bankapp.auth.application.verification_complete.port.out;

import bankapp.auth.application.shared.port.out.dto.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

import java.util.List;

public interface CredentialOptionsPort {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, byte[] defaultChallenge);
    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(List<CredentialRecord> userCredentials, byte[] challenge);
}
