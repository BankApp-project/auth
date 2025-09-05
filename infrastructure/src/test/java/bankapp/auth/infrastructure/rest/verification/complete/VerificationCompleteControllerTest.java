package bankapp.auth.infrastructure.rest.verification.complete;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification_complete.CompleteVerificationUseCase;
import bankapp.auth.application.verification_complete.port.in.CompleteVerificationCommand;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.infrastructure.rest.verification.complete.dto.CompleteVerificationRequest;
import bankapp.auth.infrastructure.rest.verification.complete.dto.CompleteVerificationResponseDto;
import bankapp.auth.infrastructure.rest.verification.complete.dto.VerificationResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(VerificationCompleteController.class)
class VerificationCompleteControllerTest {

    public static final String VERIFICATION_COMPLETE_ENDPOINT = "/verification/complete/email/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompleteVerificationUseCase completeVerificationUseCase;

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
