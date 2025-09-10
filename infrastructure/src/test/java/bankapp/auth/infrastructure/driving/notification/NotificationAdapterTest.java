package bankapp.auth.infrastructure.driving.notification;

import bankapp.auth.application.shared.port.out.NotificationPort;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NotificationAdapterTest {

    public static final EmailAddress EMAIL = new EmailAddress("test@bankapp.online");
    public static final String OTP = "123456";

    @Mock
    private NotificationTemplateProvider templateProvider;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    private NotificationPort notificationAdapter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        notificationAdapter = new NotificationAdapter(templateProvider, notificationCommandPublisher);
    }

    @Test
    void should_load_template_for_given_method() {
        notificationAdapter.sendOtpToUserEmail(EMAIL, OTP);
        verify(templateProvider).getOtpEmailTemplate(eq(OTP));
    }

    @Test
    void should_publish_msg_with_valid_data() {
        var template = "Welcome! Your OTP: " + OTP;
        when(templateProvider.getOtpEmailTemplate(eq(OTP))).thenReturn(template);
        notificationAdapter.sendOtpToUserEmail(EMAIL, OTP);

        verify(notificationCommandPublisher).publishSendEmailCommand(argThat(cmd -> {
            boolean param1 = cmd.recipientEmail().equals(EMAIL.getValue());
            boolean param2 = cmd.htmlBody().equals(template);

            return param1 && param2;
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
