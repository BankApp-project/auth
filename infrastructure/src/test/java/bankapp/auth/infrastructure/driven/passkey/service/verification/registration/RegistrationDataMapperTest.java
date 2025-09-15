package bankapp.auth.infrastructure.driven.passkey.service.verification.registration;

import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.util.UUIDUtil;
import org.junit.jupiter.api.*;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationDataMapperTest {

    private final RegistrationDataMapper mapper = new RegistrationDataMapper();
    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("Happy Path Scenario")
    class HappyPathTest {

        @Test
        @DisplayName("Should map all fields correctly from a valid RegistrationData object")
        void toDomainEntity_withValidData_mapsCorrectly() throws Exception {
            // ARRANGE
            byte[] challengeBytes = new byte[32];
            new SecureRandom().nextBytes(challengeBytes);
            String registrationResponseJson = WebAuthnTestHelper.generateValidRegistrationResponseJSON(challengeBytes);
            WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
            RegistrationData registrationData = webAuthnManager.parseRegistrationResponseJSON(registrationResponseJson);

            // ACT
            Passkey result = mapper.toDomainEntity(registrationData, userId);

            // ASSERT
            assertThat(result).isNotNull();
            Assertions.assertNotNull(registrationData.getAttestationObject());
            AttestedCredentialData attestedCredData = registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData();
            assertThat(attestedCredData).isNotNull();
            assertThat(result.getId()).isEqualTo(UUIDUtil.fromBytes(attestedCredData.getCredentialId()));
            assertThat(result.getUserHandle()).isEqualTo(userId);
            assertThat(result.getPublicKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Unit tests for invalid or edge-case data")
    class UnitTests {

        // Declare mock fields that will be shared across tests in this nested class
        private RegistrationData mockData;
        private AttestationObject mockAttestationObject;
        private AuthenticatorData<RegistrationExtensionAuthenticatorOutput> mockAuthData;
        private AttestedCredentialData mockAttestedCredData;
        private COSEKey mockCoseKey;
        private PublicKey mockPublicKey;

        @BeforeEach
        void setUp() {
            // Initialize fresh mocks before each test
            mockData = mock(RegistrationData.class);
            mockAttestationObject = mock(AttestationObject.class);
            mockAuthData = mock(AuthenticatorData.class);
            mockAttestedCredData = mock(AttestedCredentialData.class);
            mockCoseKey = mock(COSEKey.class);
            mockPublicKey = mock(PublicKey.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException if AttestationObject is null")
        void toDomainEntity_whenAttestationObjectIsNull_throwsNPE() {
            when(mockData.getAttestationObject()).thenReturn(null);

            var exception = assertThrows(NullPointerException.class, () -> mapper.toDomainEntity(mockData, userId));
            assertEquals("Attestation object cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException if AttestedCredentialData is null")
        void toDomainEntity_whenAttestedCredentialDataIsNull_throwsNPE() {
            when(mockData.getAttestationObject()).thenReturn(mockAttestationObject);
            when(mockAttestationObject.getAuthenticatorData()).thenReturn(mockAuthData);
            when(mockAuthData.getAttestedCredentialData()).thenReturn(null);

            var exception = assertThrows(NullPointerException.class, () -> mapper.toDomainEntity(mockData, userId));
            assertEquals("Attested credential data cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException if Public Key is null")
        void toDomainEntity_whenPublicKeyIsNull_throwsNPE() {
            when(mockData.getAttestationObject()).thenReturn(mockAttestationObject);
            when(mockAttestationObject.getAuthenticatorData()).thenReturn(mockAuthData);
            when(mockAuthData.getAttestedCredentialData()).thenReturn(mockAttestedCredData);
            when(mockAttestedCredData.getCOSEKey()).thenReturn(mockCoseKey);
            when(mockCoseKey.getPublicKey()).thenReturn(null);

            assertThrows(NullPointerException.class, () -> mapper.toDomainEntity(mockData, userId));
        }

        @Test
        @DisplayName("Should throw NullPointerException if Transports are null")
        void toDomainEntity_whenTransportsAreNull_throwsNPE() {

            // ARRANGE: Stub just enough to pass the initial checks
            when(mockData.getAttestationObject()).thenReturn(mockAttestationObject);
            when(mockAttestationObject.getAuthenticatorData()).thenReturn(mockAuthData);
            when(mockAuthData.getAttestedCredentialData()).thenReturn(mockAttestedCredData);
            when(mockAttestedCredData.getCredentialId()).thenReturn(new byte[16]);
            when(mockAttestedCredData.getCOSEKey()).thenReturn(mockCoseKey);
            when(mockCoseKey.getPublicKey()).thenReturn(mockPublicKey);

            // The point of the test:
            when(mockData.getTransports()).thenReturn(null);

            var exception = assertThrows(NullPointerException.class, () -> mapper.toDomainEntity(mockData, userId));
            assertEquals("Transports cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return an empty list when input transports set is empty")
        void toDomainEntity_whenTransportsAreEmpty_mapsToEmptyList() {

            // ARRANGE: Stub the full happy path
            AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> mockExtensions = mock(AuthenticationExtensionsAuthenticatorOutputs.class);
            when(mockData.getAttestationObject()).thenReturn(mockAttestationObject);
            when(mockAttestationObject.getAuthenticatorData()).thenReturn(mockAuthData);
            when(mockAuthData.getAttestedCredentialData()).thenReturn(mockAttestedCredData);
            when(mockAttestedCredData.getCredentialId()).thenReturn(new byte[16]);
            when(mockAttestedCredData.getCOSEKey()).thenReturn(mockCoseKey);
            when(mockCoseKey.getPublicKey()).thenReturn(mockPublicKey);
            when(mockPublicKey.getEncoded()).thenReturn(new byte[32]);
            when(mockAuthData.getExtensions()).thenReturn(mockExtensions);
            when(mockData.getAttestationObjectBytes()).thenReturn(new byte[]{1, 2, 3});
            when(mockData.getCollectedClientDataBytes()).thenReturn(new byte[]{4, 5, 6});

            // The point of the test:
            when(mockData.getTransports()).thenReturn(Collections.emptySet());

            // ACT
            Passkey result = mapper.toDomainEntity(mockData, userId);

            // ASSERT
            assertThat(result.getTransports()).isNotNull().isEmpty();
        }
    }
}
