package bankapp.auth.infrastructure.rest.verification;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification_complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.infrastructure.rest.verification.dto.CompleteVerificationRequest;
import bankapp.auth.infrastructure.rest.verification.dto.CompleteVerificationResponseDto;
import bankapp.auth.infrastructure.rest.verification.dto.InitiateVerificationRequest;
import bankapp.auth.infrastructure.rest.verification.dto.VerificationResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(VerificationController.class)
class VerificationControllerTest {

    public static final String VERIFICATION_INITIATE_ENDPOINT = "/verification/initiate/email/";
    public static final String VERIFICATION_COMPLETE_ENDPOINT = "/verification/complete/email/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AsyncInitiateVerificationService asyncInitiateVerificationService;

    @MockitoBean
    private CompleteVerificationUseCase completeVerificationUseCase;

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
        verify(asyncInitiateVerificationService).handleInitiation(any(InitiateVerificationCommand.class));
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

    @Test
    void completeEmailVerification_whenSuccessful_shouldReturnResponseAnd200() throws Exception {
        // Arrange
        var request = new CompleteVerificationRequest("test@bankapp.online", "123123");
        UUID challengeId = UUID.randomUUID();
        LoginResponse mockResponse = new LoginResponse(
                new PublicKeyCredentialRequestOptions(
                        new byte[]{123}, 50L, "bankapp.online", null, null, null
                ), challengeId);

        when(completeVerificationUseCase.handle(any(CompleteVerificationCommand.class)))
                .thenReturn(mockResponse);

        // 1. Create the DTO that we expect the controller to produce.
        // THIS IS THE EXPECTED RESULT.
        CompleteVerificationResponseDto expectedDto = VerificationResponseMapper.toDto(mockResponse);

        // 2. Serialize our expected DTO into a JSON string.
        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // Act & Assert
        mockMvc.perform(post(VERIFICATION_COMPLETE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // 3. Assert that the response body's JSON matches our expected JSON.
                .andExpect(content().json(expectedJson));
    }

}
