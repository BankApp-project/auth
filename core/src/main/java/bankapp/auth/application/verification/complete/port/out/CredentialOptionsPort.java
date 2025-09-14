package bankapp.auth.application.verification.complete.port.out;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.Nullable;

import java.util.List;

public interface CredentialOptionsPort {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Session session);

    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, Session session);

    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Session session);
}
