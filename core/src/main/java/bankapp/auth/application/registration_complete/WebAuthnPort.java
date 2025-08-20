package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthSession;
import bankapp.auth.application.shared.port.out.dto.CredentialRecord;

public interface WebAuthnPort {

    CredentialRecord verify(String publicKeyCredentialJson, AuthSession sessionData);
}
