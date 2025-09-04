package bankapp.auth.infrastructure.persistance.passkey;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.WithPostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@ActiveProfiles("test-postgres")
public class PostgresPasskeyRepositoryTest implements WithPostgresContainer {

    public static final byte[] CREDENTIAL_ID = "credential-id-123".getBytes();

    @Autowired
    private PostgresPasskeyRepository repo;

    @Test
    public void should_return_list_of_passkeys_of_saved_elements() {
        var registrationData = createSampleRegistrationData();

        repo.save(registrationData);

        Passkey loadedPasskey = repo.load(CREDENTIAL_ID);

        assertNotNull(loadedPasskey);
        assertEquals(registrationData.id(), loadedPasskey.getId());
        assertThat(loadedPasskey)
                .usingRecursiveComparison()
                .isEqualTo(registrationData);

    }

    private PasskeyRegistrationData createSampleRegistrationData() {
        return new PasskeyRegistrationData(
                CREDENTIAL_ID,
                UUID.randomUUID(),
                "public-key",
                "public-key-bytes".getBytes(),
                100L,
                true,
                true,
                true,
                Arrays.asList(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.USB),
                Collections.singletonMap("extKey", "extValue"),
                "attestation-object-bytes".getBytes(),
                "client-data-json-bytes".getBytes()
        );
    }
}