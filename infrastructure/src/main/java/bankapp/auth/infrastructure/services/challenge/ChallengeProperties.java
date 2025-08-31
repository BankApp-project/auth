package bankapp.auth.infrastructure.services.challenge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config.challenge")
public record ChallengeProperties(int length) {

}
