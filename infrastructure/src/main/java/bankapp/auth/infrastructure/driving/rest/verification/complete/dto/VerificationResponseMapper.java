package bankapp.auth.infrastructure.driving.rest.verification.complete.dto;

import bankapp.auth.application.shared.port.out.dto.LoginResponse;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.port.out.dto.RegistrationResponse;
import bankapp.auth.application.verification.complete.CompleteVerificationResponse;

import java.util.UUID;

public class VerificationResponseMapper {

    public static CompleteVerificationResponseDto toDto(CompleteVerificationResponse domainResponse) {
        if (domainResponse instanceof LoginResponse(
                PublicKeyCredentialRequestOptions options,
                UUID sessionId
        )) {
            return new LoginResponseDto(options, sessionId);
        }
        if (domainResponse instanceof RegistrationResponse(
                PublicKeyCredentialCreationOptions options,
                UUID sessionId
        )) {
            return new RegistrationResponseDto(options, sessionId);
        }
        // Or throw an exception if the type is unknown
        throw new IllegalArgumentException("Unknown response type: " + domainResponse.getClass().getName());
    }
}
