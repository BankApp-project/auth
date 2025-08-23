package bankapp.auth.application.verification_complete.port.out;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.domain.model.annotations.Nullable;

import java.util.List;

public interface CredentialOptionsPort {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Challenge challenge);
    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, byte[] challenge);
}
