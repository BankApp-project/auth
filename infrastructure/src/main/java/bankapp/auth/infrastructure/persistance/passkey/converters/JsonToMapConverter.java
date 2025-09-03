package bankapp.auth.infrastructure.persistance.passkey.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Converter
@RequiredArgsConstructor
public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(@NonNull Map<String, Object> stringObjectMap) {

        try {
            return objectMapper.writeValueAsString(stringObjectMap);
        } catch (JsonProcessingException e) {
            throw new JsonConverterException("Failed to convert given Map to JSON: " + stringObjectMap);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(@NonNull String dbValues) {
        try {
            return objectMapper.readValue(dbValues, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new JsonConverterException("Failed to deserialize given values: " + dbValues);
        }
    }
}
