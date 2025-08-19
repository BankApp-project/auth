package bankapp.auth.application.registration_complete;

import bankapp.auth.application.shared.port.out.dto.AuthSession;

public interface WebAuthnPort {

    boolean verify(String publicKeyCredentialJson, AuthSession sessionData);
}
