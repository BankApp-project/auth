package bankapp.auth.infrastructure.driving.rest.serializers;

import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class UserVerificationRequirementDeserializer extends JsonDeserializer<UserVerificationRequirement> {
    @Override
    public UserVerificationRequirement deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return UserVerificationRequirement.fromValue(value);
    }
}
