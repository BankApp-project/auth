package bankapp.auth.rest.verification.dto;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification_complete.CompleteVerificationResponse;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;
import bankapp.auth.application.verification_complete.port.out.dto.RegistrationResponse;

import java.util.UUID;

public class VerificationResponseMapper {

    public static CompleteVerificationResponseDto toDto(CompleteVerificationResponse domainResponse) {
        if (domainResponse instanceof LoginResponse(
                PublicKeyCredentialRequestOptions options,
                UUID challengeId
        )) {
            return new LoginResponseDto(options, challengeId);
        }
        if (domainResponse instanceof RegistrationResponse(
                PublicKeyCredentialCreationOptions options,
                UUID challengeId
        )) {
            return new RegistrationResponseDto(options, challengeId);
        }
        // Or throw an exception if the type is unknown
        throw new IllegalArgumentException("Unknown response type: " + domainResponse.getClass().getName());
    }
}
