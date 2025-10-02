package bankapp.auth.infrastructure.driven.passkey.service.options;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.service.PasskeyOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.Nullable;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyRpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * A service that acts as a facade to generate credential options for WebAuthn ceremonies.
 * It delegates the complex construction logic to specialized assembler classes.
 */
@Slf4j
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
        log.info("Generating passkey creation options.");

        PasskeyCreationOptionsAssembler assembler = new PasskeyCreationOptionsAssembler(properties, clock);
        PublicKeyCredentialCreationOptions options = assembler.assemble(user, session);

        log.info("Successfully generated passkey creation options.");
        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Session session) {
        log.info("Generating passkey request options without user credentials.");
        return getPasskeyRequestOptions(null, session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, Session session) {
        log.info("Generating passkey request options.");

        PasskeyRequestOptionsAssembler assembler = new PasskeyRequestOptionsAssembler(properties, clock);
        PublicKeyCredentialRequestOptions options = assembler.assemble(userCredentials, session);

        log.info("Successfully generated passkey request options.");
        return options;
    }
}