package bankapp.auth.services;

import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class NotificationTemplateProviderTest {


    @Test
    void should_generate_template_for_given_request() {
        //When
        var notificationTemplateProvider = new NotificationTemplateProvider();

        var email = new EmailAddress("test@bankapp.online");
        var otp = "123123";
        var res = notificationTemplateProvider.getOtpEmailTemplate(otp);

        assertThat(res).contains(otp);
        assertThat(res).contains("Hello");
        assertThat(res).contains("Your One-Time Password is:");
    }

}