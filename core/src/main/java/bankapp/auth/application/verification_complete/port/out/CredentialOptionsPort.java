package bankapp.auth.application.verification_complete.port.out;

import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;

import java.util.List;

public interface CredentialOptionsPort {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, byte[] challenge);
    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(List<Passkey> userCredentials, byte[] challenge);
}
