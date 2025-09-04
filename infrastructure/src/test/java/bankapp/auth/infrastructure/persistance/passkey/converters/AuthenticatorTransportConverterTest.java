package bankapp.auth.infrastructure.persistance.passkey.converters;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuthenticatorTransportConverterTest {

    @Test
    public void should_correctly_serialize_and_deserialize_valid_values() {

        var testValues = Arrays.asList(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.USB);

        var converter = new AuthenticatorTransportConverter();

        var valuesString = converter.convertToDatabaseColumn(testValues);
        var res = converter.convertToEntityAttribute(valuesString);

        assertThat(res)
                .usingRecursiveComparison()
                .isEqualTo(testValues);
    }

}