package bankapp.auth.infrastructure.driven.passkey.config;

import com.webauthn4j.data.client.Origin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasskeyConfiguration {

    private final PasskeyRpProperties passkeyRpProperties;
    private final PasskeySecurityProperties passkeySecurityProperties;

    public Origin origin() {
        String originHttps = "https://" + passkeyRpProperties.rpId();
        return new Origin(originHttps);
    }

    public String rpId() {
        return passkeyRpProperties.rpId();
    }

    public boolean userVerificationRequired() {
        return passkeySecurityProperties.userVerificationRequired();
    }

    public boolean userPresenceRequired() {
        return passkeySecurityProperties.userPresenceRequired();
    }
}
