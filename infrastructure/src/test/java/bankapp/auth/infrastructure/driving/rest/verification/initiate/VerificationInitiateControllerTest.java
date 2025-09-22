package bankapp.auth.infrastructure.driving.rest.verification.initiate;

import bankapp.auth.application.verification.initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.infrastructure.crosscutting.config.SpringSecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationInitiateController.class)
@Import(SpringSecurityConfiguration.class)
public class VerificationInitiateControllerTest {

    public static final String VERIFICATION_INITIATE_ENDPOINT = "/verification/initiate/email";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AsyncInitiateVerificationService asyncInitiateVerificationService;

    @Test
    void should_return_202_when_provided_valid_email() throws Exception {
        var request = new InitiateVerificationRequest("test@bankapp.online");

        // Act & Assert using MockMvc
        mockMvc.perform(post(VERIFICATION_INITIATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("")); // Expect an empty body

        // 4. Verify the controller called the async service.
        verify(asyncInitiateVerificationService).handle(any(InitiateVerificationCommand.class));
    }

    @Test
    void should_return_400_when_provided_invalid_email() throws Exception {
        var request = new InitiateVerificationRequest("test-bankapp.online");

        // Act & Assert
        // The controller will throw an InvalidEmailFormatException when creating the EmailAddress.
        // The GlobalExceptionHandler (auto-detected by @WebMvcTest) will catch it and return 400.
        mockMvc.perform(post(VERIFICATION_INITIATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the background task was never even started
        verifyNoInteractions(asyncInitiateVerificationService);
    }
}
