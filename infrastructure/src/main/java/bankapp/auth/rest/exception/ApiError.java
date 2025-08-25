package bankapp.auth.rest.exception;

import org.springframework.http.HttpStatus;

public record ApiError(
        HttpStatus errorCode,
        String message
) {
}
