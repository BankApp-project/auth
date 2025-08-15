package bankapp.auth.domain.service;

import bankapp.auth.domain.service.stubs.StubChallengeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void should_return_LoginResponse_with_at_least_16bytes_long_challenge() {
        // Given

        // When
        var res = passkeyOptionsService.getLoginResponse();

        // Then
        byte[] challenge = res.options().challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_unique_LoginResponse() {

        // When
        var res1 = passkeyOptionsService.getLoginResponse();
        var res2 = passkeyOptionsService.getLoginResponse();

        // Then
        byte[] challenge1 = res1.options().challenge();
        byte[] challenge2 = res2.options().challenge();

        assertFalse(java.util.Arrays.equals(challenge1, challenge2), "Challenges should be unique");
    }
}
