package bankapp.auth.rest.authentication;

import bankapp.auth.application.authentication_initiate.InitiateAuthenticationCommand;
import bankapp.auth.application.authentication_initiate.InitiateAuthenticationUseCase;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @MockitoBean
    InitiateAuthenticationUseCase initiateAuthenticationUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_return_response_with_challengeId_and_PublicKeyCredentialRequestOptions() throws Exception {

        var command = new InitiateAuthenticationCommand();
        var options = new PublicKeyCredentialRequestOptions(
                new byte[]{123},
                null,
                null,
                null,
                null,
                null
        );
        var loginResponse = new LoginResponse(options, UUID.randomUUID());

        when(initiateAuthenticationUseCase.handle(command)).thenReturn(loginResponse);

        var responseStub = new InitiateAuthenticationResponse(options, loginResponse.challengeId().toString());
        var responseJson = objectMapper.writeValueAsString(responseStub);


        mockMvc.perform(get("/authentication/initiate")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }
}