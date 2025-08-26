package bankapp.auth.services;

import bankapp.auth.application.shared.port.out.NotificationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class NotificationAdapterTest {

    public static final EmailAddress EMAIL = new EmailAddress("test@bankapp.online");
    public static final String OTP = "123456";

    @Mock
    private NotificationTemplateProvider templateProvider;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // make template
    // add data to template
    @Test
    void should_load_template_for_given_method() {
        NotificationPort notificationAdapter = new NotificationAdapter(templateProvider);
        notificationAdapter.sendOtpToUserEmail(EMAIL, OTP);
        verify(templateProvider).getOtpEmailTemplate(eq(OTP));
    }

    // NEXT STEPS
    // save data as json payload
    // send payload as command to notification-service (amqp? but as command, not on the queue)
}