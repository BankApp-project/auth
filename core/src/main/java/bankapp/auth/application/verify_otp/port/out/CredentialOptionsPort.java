package bankapp.auth.application.verify_otp.port.out;

import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

import java.util.List;

public interface CredentialOptionsPort {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, byte[] defaultChallenge);
    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(User user, List<CredentialRecord> userCredentials, byte[] challenge);
}
