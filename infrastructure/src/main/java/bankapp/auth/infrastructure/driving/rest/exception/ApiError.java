package bankapp.auth.infrastructure.driving.rest.exception;

import org.springframework.http.HttpStatus;

public record ApiError(
        HttpStatus errorCode,
        String message
) {
}
