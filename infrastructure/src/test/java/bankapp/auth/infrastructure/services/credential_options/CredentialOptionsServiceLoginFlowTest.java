package bankapp.auth.infrastructure.services.credential_options;

import bankapp.auth.application.shared.enums.AuthMode;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CredentialOptionsServiceLoginFlowTest {


    private static final AuthMode DEFAULT_AUTH_MODE = AuthMode.SMARTPHONE;
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final Clock DEFAULT_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final EmailAddress DEFAULT_EMAIL_ADDRESS = new EmailAddress("test@bankapp.online");
    private static final User DEFAULT_USER = User.createNew(DEFAULT_EMAIL_ADDRESS);
    private static final List<Passkey> DEFAULT_USER_CREDENTIALS = List.of(new Passkey(
            UUID.randomUUID(),
            DEFAULT_USER.getId(),
            null,
            0L,
            false,
            false,
            null
    ));
    private static final Challenge DEFAULT_CHALLENGE = new Challenge(
            UUID.randomUUID(),
            ByteArrayUtil.uuidToBytes(UUID.randomUUID()),
            DEFAULT_TIMEOUT,
            DEFAULT_CLOCK
    );

    CredentialOptionsService passkeyOptionsService;

    @BeforeEach
    void setup() {
        var passkeyOptionsProperties = new CredentialOptionsProperties(
                DEFAULT_RPID,
                DEFAULT_AUTH_MODE
        );

        passkeyOptionsService = new CredentialOptionsService(
                passkeyOptionsProperties,
                DEFAULT_CLOCK
        );
    }

    @Test
    void should_return_response_with_at_least_16bytes_long_challenge() {

        // When
        var res = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE);

        // Then
        byte[] challenge = res.challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_response_with_default_timeout() {
        var timeout = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).timeout();

        assertNotNull(timeout);
        assertEquals(DEFAULT_TIMEOUT.toMillis(), timeout);
    }

    @Test
    void should_return_response_with_default_rpid() {
        var rpId = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).rpId();

        assertNotNull(rpId);
        assertEquals(DEFAULT_RPID, rpId);
    }

    @Test
    void should_return_response_with_allowedCredentials_list_corresponding_to_given_user() {

        var allowedCredentials = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).allowCredentials();

        assertNotNull(allowedCredentials);
        var credentialIdBytes = ByteArrayUtil.bytesToUuid(allowedCredentials.getFirst().id());
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().getId(), credentialIdBytes);
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().getTransports(), allowedCredentials.getFirst().transports());
    }

    @Test
    void should_return_response_with_userVerification_set_to_required() {
        var uv = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).userVerification();

        assertNotNull(uv);
        assertEquals("required", uv.getValue());
    }

    @Test
    void should_return_empty_allowed_credential_list_when_userCredentials_is_null() {
        var res = passkeyOptionsService.getPasskeyRequestOptions(null, DEFAULT_CHALLENGE);

        assertEquals(new ArrayList<>(), res.allowCredentials());
    }
}
