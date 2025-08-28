package bankapp.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.config.otp")
public record OtpProperties(
        int size,
        Duration ttl
) {}