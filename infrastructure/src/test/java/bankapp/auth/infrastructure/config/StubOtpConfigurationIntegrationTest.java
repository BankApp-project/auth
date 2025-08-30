package bankapp.auth.infrastructure.config;

import bankapp.auth.infrastructure.persistance.otp.config.OtpConfiguration;
import bankapp.auth.infrastructure.persistance.otp.config.OtpProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EnableConfigurationProperties(OtpProperties.class)
@SpringBootTest(classes = {OtpConfiguration.class, ClockConfiguration.class})
@TestPropertySource(properties = {
        "app.config.otp.size=8",
        "app.config.otp.ttl=300s"
})
class StubOtpConfigurationIntegrationTest {


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
