package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import com.webauthn4j.converter.util.ObjectConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebAuthnMapperTest {

    private WebAuthnMapper webAuthnMapper;

    @BeforeEach
    void setUp() {
        var objectConverter = new ObjectConverter();
        webAuthnMapper = new WebAuthnMapper(objectConverter);
    }

    @Nested
    class MapToClientExtensionsTests {

        @Test
            // RENAMED TEST METHOD
        void mapToClientExtensions_should_returnNull_when_inputMapIsNull() {
            // When
            var result =
                    webAuthnMapper.mapToClientExtensions(null); // Updated call

            // Then
            assertThat(result).isNull();
        }

        @Test
            // RENAMED TEST METHOD
        void mapToClientExtensions_should_returnEmptyObject_when_inputMapIsEmpty() {
            // When
            var result =
                    webAuthnMapper.mapToClientExtensions(Collections.emptyMap()); // Updated call

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getKeys()).isEmpty();
        }

        @Test
        void mapToClientExtensions_should_returnPopulatedObject_when_inputMapContainsAppIdExtension() {
            // Given: A simple extension with a single boolean value.
            HashMap<String, Object> extensionData = new HashMap<>();
            extensionData.put("appid", true);

            // When
            var result = webAuthnMapper.mapToClientExtensions(extensionData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppid()).isTrue();
        }

        @Test
            // RENAMED TEST METHOD
        void mapToClientExtensions_should_storeUnknowns_when_inputMapContainsUnknownExtensions() {
            // Given
            Map<String, Object> customExtensionValue = Map.of("someKey", "someValue");
            Map<String, Object> extensionData = Map.of("customExtension", customExtensionValue);

            // When
            var result =
                    webAuthnMapper.mapToClientExtensions(extensionData); // Updated call

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUnknownKeys()).containsExactly("customExtension");
            assertThat(result.getValue("customExtension")).isEqualTo(customExtensionValue);
        }
    }

    @Nested
    class MapToWebAuthnTransportsTests {
        // No changes in this nested class
        @Test
        void mapToWebAuthnTransports_should_returnEmptySet_when_inputListIsNull() {
            Set<com.webauthn4j.data.AuthenticatorTransport> result = webAuthnMapper.mapToWebAuthnTransports(null);
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void mapToWebAuthnTransports_should_returnEmptySet_when_inputListIsEmpty() {
            Set<com.webauthn4j.data.AuthenticatorTransport> result = webAuthnMapper.mapToWebAuthnTransports(Collections.emptyList());
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void mapToWebAuthnTransports_should_returnCorrectlyMappedSet_when_inputListContainsValidTransports() {
            List<AuthenticatorTransport> appTransports = List.of(AuthenticatorTransport.USB, AuthenticatorTransport.INTERNAL);
            Set<com.webauthn4j.data.AuthenticatorTransport> result = webAuthnMapper.mapToWebAuthnTransports(appTransports);
            assertThat(result).containsExactlyInAnyOrder(com.webauthn4j.data.AuthenticatorTransport.USB, com.webauthn4j.data.AuthenticatorTransport.INTERNAL);
        }

        @Test
        void mapToWebAuthnTransports_should_returnSetWithoutDuplicates_when_inputListContainsDuplicates() {
            List<AuthenticatorTransport> appTransports = List.of(AuthenticatorTransport.USB, AuthenticatorTransport.NFC, AuthenticatorTransport.USB);
            Set<com.webauthn4j.data.AuthenticatorTransport> result = webAuthnMapper.mapToWebAuthnTransports(appTransports);
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(com.webauthn4j.data.AuthenticatorTransport.USB, com.webauthn4j.data.AuthenticatorTransport.NFC);
        }

        @Test
        void mapToWebAuthnTransports_should_throwIllegalArgumentException_when_inputListContainsUnsupportedTransport() {
            List<AuthenticatorTransport> appTransports = List.of(AuthenticatorTransport.SMART_CARD);
            assertThatThrownBy(() -> webAuthnMapper.mapToWebAuthnTransports(appTransports))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("smart-card");
        }
    }
}
