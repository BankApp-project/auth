package bankapp.auth.infrastructure.driving.rest.exception.handler;

import org.springframework.http.HttpStatus;

public record ApiError(
        HttpStatus errorCode,
        String message
) {
}
