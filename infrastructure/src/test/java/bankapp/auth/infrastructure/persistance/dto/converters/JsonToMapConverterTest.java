package bankapp.auth.infrastructure.persistance.dto.converters;

import bankapp.auth.infrastructure.config.JSONConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class JsonToMapConverterTest {

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

}