package bankapp.auth.infrastructure.driving.rest.authentication.initiate;

import bankapp.auth.application.authentication.initiate.InitiateAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.LoginResponse;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.infrastructure.crosscutting.config.SpringSecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InitiateAuthenticationController.class)
@Import(SpringSecurityConfiguration.class)
class InitiateAuthenticationControllerTest {

    @MockitoBean
    private InitiateAuthenticationUseCase initiateAuthenticationUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_return_response_with_sessionId_and_PublicKeyCredentialRequestOptions() throws Exception {
        // Given
        var options = new PublicKeyCredentialRequestOptions(
                new byte[]{123},
                null,
                null,
                null,
                null,
                null
        );
        var loginResponse = new LoginResponse(options, UUID.randomUUID());

        when(initiateAuthenticationUseCase.handle()).thenReturn(loginResponse);

        var responseStub = new InitiateAuthenticationResponse(options, loginResponse.sessionId().toString());
        var responseJson = objectMapper.writeValueAsString(responseStub);

        // When & Then
        mockMvc.perform(get("/authentication/initiate")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

        verify(initiateAuthenticationUseCase).handle();
    }
}
