package bankapp.auth.services;

import bankapp.auth.application.shared.enums.AuthMode;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class CredentialOptionsServiceLoginFlowTest {


    private static final AuthMode DEFAULT_AUTH_MODE = AuthMode.SMARTPHONE;
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    private static final long DEFAULT_TIMEOUT = 30000; //30s in ms
    private static final EmailAddress DEFAULT_EMAIL_ADDRESS = new EmailAddress("test@bankapp.online");
    private static final User DEFAULT_USER = new User(DEFAULT_EMAIL_ADDRESS);
    private static final List<Passkey> DEFAULT_USER_CREDENTIALS = List.of(new Passkey(
                ByteArrayUtil.uuidToBytes(UUID.randomUUID()),
                DEFAULT_USER.getId(),
            null,
                0L,
                false,
            false,
                null
    ));
    private static final Challenge DEFAULT_CHALLENGE = new Challenge(
            UUID.randomUUID(),
            new byte[]{123},
            DEFAULT_TIMEOUT / 1000,
            DEFAULT_CLOCK
    );

    CredentialOptionsService passkeyOptionsService;

    @BeforeEach
    void setup() {
        passkeyOptionsService = new CredentialOptionsService(
                DEFAULT_AUTH_MODE,
                DEFAULT_RPID,
                DEFAULT_TIMEOUT
        );
    }

    @Test
    void should_return_response_with_at_least_16bytes_long_challenge() {
        // Given

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
        assertEquals(DEFAULT_TIMEOUT, timeout);
    }

    @Test
    void should_return_response_with_default_rpid() {
        var rpId = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).rpId();

        assertNotNull(rpId);
        assertEquals(DEFAULT_RPID,rpId);
    }

    @Test
    void should_return_response_with_allowedCredentials_list_corresponding_to_given_user() {

        var allowedCredentials = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).allowCredentials();

        assertNotNull(allowedCredentials);
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().getId() ,allowedCredentials.getFirst().id());
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().getTransports(),allowedCredentials.getFirst().transports());
    }

    @Test
    void should_return_response_with_userVerification_set_to_required() {
        var uv = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER_CREDENTIALS, DEFAULT_CHALLENGE).userVerification();

        assertNotNull(uv);
        assertEquals("required", uv.getValue());
    }

    @Test
    void should_return_empty_allowed_credential_list_when_userCredentials_is_null () {
        var res = passkeyOptionsService.getPasskeyRequestOptions(null,DEFAULT_CHALLENGE);

        assertEquals(new ArrayList<>(), res.allowCredentials());
    }
}
