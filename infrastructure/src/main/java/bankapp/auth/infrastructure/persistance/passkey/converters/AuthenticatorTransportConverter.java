package bankapp.auth.infrastructure.persistance.passkey.converters;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class AuthenticatorTransportConverter implements AttributeConverter<List<AuthenticatorTransport>, String> {

    private final static String DELIMITER = ";";

    @Override
    public String convertToDatabaseColumn(List<AuthenticatorTransport> authenticatorTransports) {
        return authenticatorTransports.stream()
                .map(AuthenticatorTransport::getValue)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<AuthenticatorTransport> convertToEntityAttribute(String s) {
        return Arrays.stream(s.split(DELIMITER))
                .map(AuthenticatorTransport::fromValue)
                .toList();
    }
}
