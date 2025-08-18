package bankapp.auth.domain.service;

import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

import java.util.List;

public interface PasskeyOptionsService {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user);
    PublicKeyCredentialRequestOptions getPasskeyRequestOptions(User user, List<CredentialRecord> userCredentials);
}
