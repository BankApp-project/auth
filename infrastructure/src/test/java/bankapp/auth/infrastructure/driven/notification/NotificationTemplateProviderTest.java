package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.notification.exception.InvalidEmailTemplateArgumentException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class NotificationTemplateProviderTest {

    private final EmailAddress DEFAULT_EMAIL = new EmailAddress("test@bankapp.online");
    private final String DEFAULT_OTP = "123456";
    private final NotificationTemplateProvider notificationTemplateProvider = new NotificationTemplateProvider();

    @Test
    void getOtpEmailTemplate_should_throw_when_otp_empty() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, ""));
    }

    @Test
    void getOtpEmailTemplate_should_throw_when_otp_null() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, null));
    }

    @Test
    void getOtpEmailTemplate_should_throw_when_email_null() {
        assertThrows(InvalidEmailTemplateArgumentException.class, () -> notificationTemplateProvider.getOtpEmailTemplate(null, ""));
    }

    @Test
    void getOtpEmailTemplate_should_return_not_null_template_when_provided_valid_data() {
        var res = notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, DEFAULT_OTP);

        assertNotNull(res, "Template should not be null");
        assertNotNull(res.body(), "Template body should not be null");
        assertNotNull(res.subject(), "Template subject should not be null");
        assertNotNull(res.sendTo(), "Template sendTo should not be null");
    }

    @Test
    void getOtpEmailTemplate_should_return_template_with_otp_in_body_when_provided_valid_data() {
        var res = notificationTemplateProvider.getOtpEmailTemplate(DEFAULT_EMAIL, DEFAULT_OTP);

        assertTrue(res.body().contains(DEFAULT_OTP));
    }
}