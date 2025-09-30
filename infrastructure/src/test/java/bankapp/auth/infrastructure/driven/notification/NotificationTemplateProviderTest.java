package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class NotificationTemplateProviderTest {

    private Duration DEFAULT_DURATION = Duration.ofMinutes(5);
    private EmailAddress DEFAULT_EMAIL = new EmailAddress("test@bankapp.online");
    private String DEFAULT_OTP = "123456";
    private NotificationTemplateProvider notificationTemplateProvider = new NotificationTemplateProvider();

    @Test
    void should_generate_template_for_given_request() {
        //When

        var otp = "123123";
        var res = notificationTemplateProvider.getOtpEmailTemplate(otp);

        assertThat(res).contains(otp);
        assertThat(res).contains("Hello");
        assertThat(res).contains("Your One-Time Password is:");
    }

    @Test
    void getOtpEmailTemplate_should_throw_when_otp_empty() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, "", DEFAULT_DURATION));
    }

    @Test
    void getOtpEmailTemplate_should_throw_when_otp_null() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, null, DEFAULT_DURATION));
    }

    @Test
    void getOtpEmailTemplate_should_throw_when_email_null() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(null, "", DEFAULT_DURATION));
    }

    @Test
    void getOtpEmailTemplate_should_throw_when_duration_null() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, DEFAULT_OTP, null));
    }

    @Test
    void getOtpEmailTemplate_should_return_valid_template_when_provided_valid_data() {
        var res = notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, DEFAULT_OTP, DEFAULT_DURATION);

        assertNotNull(res, "Template should not be null");
        assertNotNull(res.body(), "Template body should not be null");
        assertNotNull(res.subject(), "Template subject should not be null");
        assertNotNull(res.sendTo(), "Template sendTo should not be null");
    }
}