package bankapp.auth.infrastructure.services.challenge;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.config.challenge")
public record ChallengeProperties(int length, Duration ttl) {

}
