package bankapp.auth.infrastructure.driven.notification;

import bankapp.auth.application.shared.port.out.service.NotificationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.notification.out.NotificationCommandPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NotificationAdapterTest {

    public static final EmailAddress EMAIL = new EmailAddress("test@bankapp.online");
    public static final String OTP = "123456";
    private static final String SUBJECT = "Test Subject";
    public static final String BODY = "testBody";

    @Mock
    private NotificationTemplateProvider templateProvider;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    private NotificationPort notificationAdapter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        notificationAdapter = new NotificationAdapter(templateProvider, notificationCommandPublisher);

        var newTemplate = new EmailTemplate(SUBJECT, BODY, EMAIL);
        when(templateProvider.getOtpEmailTemplate(eq(EMAIL), eq(OTP))).thenReturn(newTemplate);
    }

    @Test
    void should_load_template_for_given_method() {
        notificationAdapter.sendOtpToUserEmail(EMAIL, OTP);
        verify(templateProvider).getOtpEmailTemplate(eq(EMAIL), eq(OTP));
    }

    @Test
    void should_publish_msg_with_valid_data() {
        notificationAdapter.sendOtpToUserEmail(EMAIL, OTP);

        verify(notificationCommandPublisher).publishSendEmailCommand(argThat(cmd -> {
            boolean param1 = cmd.recipientEmail().equals(EMAIL.getValue());
            boolean param2 = cmd.htmlBody().equals(BODY);
            boolean param3 = cmd.subject().equals(SUBJECT);

            return param1 && param2 && param3;
        }));
    }

    @Test
    void should_throw_null_pointer_exception_when_email_is_null() {

        // Given & When & Then
        assertThrows(NullPointerException.class, () -> notificationAdapter.sendOtpToUserEmail(null, OTP));
    }

    @Test
    void should_throw_null_pointer_exception_when_otp_is_null() {

        // Given & When & Then
        assertThrows(NullPointerException.class, () -> notificationAdapter.sendOtpToUserEmail(EMAIL, null));
    }
}
