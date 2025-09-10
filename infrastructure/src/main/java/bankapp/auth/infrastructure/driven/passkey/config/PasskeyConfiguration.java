package bankapp.auth.infrastructure.driven.passkey.config;

import com.webauthn4j.data.client.Origin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasskeyConfiguration {

    private final PasskeyRpProperties passkeyRpProperties;

    public Origin origin() {
        String originHttps = "https://" + passkeyRpProperties.rpId();
        return new Origin(originHttps);
    }

    public String rpId() {
        return passkeyRpProperties.rpId();
    }
}
