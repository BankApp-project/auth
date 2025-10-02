package bankapp.auth.infrastructure.driving.rest.shared.config.serializers;

import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class UserVerificationRequirementSerializer extends JsonSerializer<UserVerificationRequirement> {

    @Override
    public void serialize(UserVerificationRequirement value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getValue());
    }
}
