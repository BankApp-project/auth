package bankapp.auth.infrastructure.persistance.dto.converters;

import bankapp.auth.infrastructure.config.JSONConfiguration;
import bankapp.auth.infrastructure.persistance.passkey.converters.JsonConverterException;
import bankapp.auth.infrastructure.persistance.passkey.converters.JsonToMapConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonToMapConverterTest {

    @Mock
    private ObjectMapper mockMapper;

    @Test
    void should_serialize_to_string_and_deserialize_to_same_object() {

        var objToMap = Integer.valueOf(321);
        var key = "banana";
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, objToMap);

        var mapper = new JSONConfiguration().objectMapper();
        var converter = new JsonToMapConverter(mapper);

        String dbEntry = converter.convertToDatabaseColumn(map);
        var persistedMap = converter.convertToEntityAttribute(dbEntry);

        assertThat(map)
                .isEqualTo(persistedMap)
                .usingRecursiveComparison();
    }

    @Test
    void convertToDatabaseColumn_should_throw_exception_when_null_values_passed() {
        var mapper = new JSONConfiguration().objectMapper();
        var converter = new JsonToMapConverter(mapper);

        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_should_throw_exception_when_serialization_fails() throws JsonProcessingException {
        when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mocked serialization error") {
        });

        var converter = new JsonToMapConverter(mockMapper);
        var testMap = new HashMap<String, Object>();
        testMap.put("test", "value");

        var exception = assertThrows(JsonConverterException.class,
                () -> converter.convertToDatabaseColumn(testMap));

        assertThat(exception.getMessage()).containsIgnoringCase("Failed");
        assertThat(exception.getMessage()).containsIgnoringCase("convert");
        assertThat(exception.getMessage()).containsIgnoringCase("json");
    }

    @Test
    void convertToEntityAttribute_should_throw_exception_when_null_values_passed() {
        var mapper = new JSONConfiguration().objectMapper();
        var converter = new JsonToMapConverter(mapper);

        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_should_throw_exception_when_deserialization_fails() throws JsonProcessingException {
        when(mockMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("Mocked deserialization error") {});


        var converter = new JsonToMapConverter(mockMapper);

        var exception = assertThrows(JsonConverterException.class,
                () -> converter.convertToEntityAttribute("{\"test\": \"value\"}"));

        assertThat(exception.getMessage()).containsIgnoringCase("Failed");
        assertThat(exception.getMessage()).containsIgnoringCase("deserialize");
        assertThat(exception.getMessage()).containsIgnoringCase("values");
    }
}
