package bankapp.auth.infrastructure.config;

import bankapp.auth.infrastructure.persistance.otp.config.OtpConfiguration;
import bankapp.auth.infrastructure.persistance.otp.config.OtpProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EnableConfigurationProperties(OtpProperties.class)
@SpringBootTest(classes = {OtpConfiguration.class, StubOtpConfigurationIntegrationTest.TestConfig.class})
@TestPropertySource(properties = {
        "app.config.otp.size=8",
        "app.config.otp.ttl=300s"
})
class StubOtpConfigurationIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.now(), ZoneId.of("UTC"));
        }
    }

    @Autowired
    private OtpConfiguration otpConfiguration;

    @Autowired
    private Clock clock;

    @Test
    void shouldLoadOtpPropertiesWithValuesFromTestProperties() {
        // Then
        assertThat(otpConfiguration).isNotNull();
        assertThat(otpConfiguration.getOtpSize()).isEqualTo(8);
        assertThat(otpConfiguration.getTtl()).isEqualTo(Duration.ofSeconds(300));
        assertThat(otpConfiguration.getClock()).isNotNull();
        assertThat(otpConfiguration.getClock()).isEqualTo(clock);
    }
}
