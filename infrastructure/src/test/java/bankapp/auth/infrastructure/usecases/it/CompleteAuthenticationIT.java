package bankapp.auth.infrastructure.usecases.it;

import bankapp.auth.application.authentication.complete.CompleteAuthenticationCommand;
import bankapp.auth.application.authentication.complete.CompleteAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.PasskeyRepository;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import bankapp.auth.infrastructure.utils.WithPostgresContainer;
import bankapp.auth.infrastructure.utils.WithRedisContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@SpringBootTest
@ActiveProfiles("test-postgres")
@Transactional
public class CompleteAuthenticationIT implements WithPostgresContainer, WithRedisContainer {

    private final static String DEFAULT_EMAIL_ADDRESS = "test@bankapp.online";
    private final static EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_EMAIL_ADDRESS);
    private final static UUID DEFAULT_SESSION_ID = UUID.randomUUID();
    private final static String DEFAULT_RP_ID = "bankapp.online";

    private TestPasskeyProvider.PasskeyInfo passkeyInfo;
    private Passkey passkey;
    private Challenge challenge;

    @Autowired
    private CompleteAuthenticationUseCase completeAuthenticationUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasskeyRepository passkeyRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ChallengeGenerationPort challengeGenerationPort;

    @BeforeEach
    void setup() {
        User user = User.createNew(DEFAULT_EMAIL);
        user.activate();
        userRepository.save(user);

        passkeyInfo = TestPasskeyProvider.createSamplePasskeyInfo(user.getId());
        passkey = passkeyInfo.passkey();
        passkeyRepository.save(passkey);

        challenge = challengeGenerationPort.generate();
    }

    @Test
    void completeAuthentication_should_return_auth_tokens_and_increment_sign_count_when_valid_request_without_user_in_session() throws Exception {
        //given
        var session = generateAndPersistSession();
        assertThat(sessionRepository.load(session.sessionId())).isPresent();
        var command = createCompleteAuthenticationCommand(session);

        //when
        var res = completeAuthenticationUseCase.handle(command);

        //then
        assertNotNull(res.authTokens(), "authTokens should not be null");
        assertNotNull(res.authTokens().refreshToken(), "refreshToken should not be null");
        assertNotNull(res.authTokens().accessToken(), "accessToken should not be null");

        checkSignCountIsGreaterThanBefore();

        //session should be deleted after operation
        assertThat(sessionRepository.load(session.sessionId())).isEmpty();
    }

    private @NotNull Session generateAndPersistSession() {
        var session = new Session(DEFAULT_SESSION_ID, challenge);
        sessionRepository.save(session);
        return session;
    }

    private @NotNull CompleteAuthenticationCommand createCompleteAuthenticationCommand(Session session) throws Exception {
        var authResponseJson = createAuthResponseJSON();
        return new CompleteAuthenticationCommand(session.sessionId(), authResponseJson, passkey.getId());
    }

    private String createAuthResponseJSON() throws Exception {
        return WebAuthnTestHelper.generateValidAuthenticationResponseJSON(
                challenge.challenge(),
                DEFAULT_RP_ID,
                getCredentialId(passkey),
                passkeyInfo.keyPair(),
                passkey.getSignCount()
        );
    }

    private byte[] getCredentialId(Passkey passkey) {
        return ByteArrayUtil.uuidToBytes(passkey.getId());
    }

    private void checkSignCountIsGreaterThanBefore() {
        var oldSignCount = passkey.getSignCount();

        var loadedPasskeyOpt = passkeyRepository.load(passkey.getId());
        assertThat(loadedPasskeyOpt).isPresent();
        var loadedPasskey = loadedPasskeyOpt.get();
        var updatedSignCount = loadedPasskey.getSignCount();

        assertThat(updatedSignCount).isGreaterThan(oldSignCount);
    }
}
