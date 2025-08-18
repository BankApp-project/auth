package bankapp.auth.domain.service;

import bankapp.auth.domain.service.stubs.StubChallengeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PasskeyOptionsServiceLoginFlowTest {


    private static final String DEFAULT_AUTH_MODE = "smartphone";
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final long DEFAULT_TIMEOUT = 30000; //30s in ms

    PasskeyOptionsServiceImpl passkeyOptionsService;

    @BeforeEach
    void setup() {
        passkeyOptionsService = new PasskeyOptionsServiceImpl(
                DEFAULT_AUTH_MODE,
                DEFAULT_RPID,
                DEFAULT_TIMEOUT,
                new StubChallengeGenerator()
        );
    }

    @Test
    void should_return_response_with_at_least_16bytes_long_challenge() {
        // Given

        // When
        var res = passkeyOptionsService.getPasskeyRequestOptions();

        // Then
        byte[] challenge = res.challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_unique_response() {

        // When
        var res1 = passkeyOptionsService.getPasskeyRequestOptions();
        var res2 = passkeyOptionsService.getPasskeyRequestOptions();

        // Then
        byte[] challenge1 = res1.challenge();
        byte[] challenge2 = res2.challenge();

        assertFalse(java.util.Arrays.equals(challenge1, challenge2), "Challenges should be unique");
    }

    @Test
    void should_return_response_with_default_timeout() {
        var timeout = passkeyOptionsService.getPasskeyRequestOptions().timeout();

        assertNotNull(timeout);
        assertEquals(DEFAULT_TIMEOUT, timeout);
    }

    @Test
    void should_return_response_with_default_rpid() {
        var rpId = passkeyOptionsService.getPasskeyRequestOptions().rpId();

        assertNotNull(rpId);
        assertEquals(DEFAULT_RPID,rpId);
    }
}
