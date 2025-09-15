package bankapp.auth.infrastructure.driven.passkey.service.options;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.verification.complete.port.out.PasskeyOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.Nullable;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyRpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * A service that acts as a facade to generate credential options for WebAuthn ceremonies.
 * It delegates the complex construction logic to specialized assembler classes.
 */
@Service
@RequiredArgsConstructor
public class PasskeyOptionsService implements PasskeyOptionsPort {

    private final PasskeyRpProperties properties;
    private final Clock clock;

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Session session) {
        PasskeyCreationOptionsAssembler assembler = new PasskeyCreationOptionsAssembler(properties, clock);
        return assembler.assemble(user, session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Session session) {
        return getPasskeyRequestOptions(null, session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, Session session) {
        PasskeyRequestOptionsAssembler assembler = new PasskeyRequestOptionsAssembler(properties, clock);
        return assembler.assemble(userCredentials, session);
    }
}