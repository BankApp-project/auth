package bankapp.auth.domain.service;

import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.User;

public interface PasskeyOptionsService {
    RegistrationResponse getRegistrationResponse(User user);
    LoginResponse getLoginResponse();
}
