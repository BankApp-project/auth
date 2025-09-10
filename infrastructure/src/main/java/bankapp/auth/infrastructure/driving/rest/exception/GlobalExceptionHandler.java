package bankapp.auth.infrastructure.driving.rest.exception;

import bankapp.auth.application.verification.complete.OtpVerificationException;
import bankapp.auth.domain.model.exception.InvalidEmailFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<ApiError> handleInvalidEmailFormatException(InvalidEmailFormatException e) {
        var status = HttpStatus.BAD_REQUEST;
        var err = new ApiError(status, e.getMessage());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e) {
        var status = HttpStatus.BAD_REQUEST;
        var err = new ApiError(status, e.getMessage());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(OtpVerificationException.class)
    public  ResponseEntity<ApiError> handleOtpVerificationException(OtpVerificationException e) {
        var status = HttpStatus.BAD_REQUEST;
        var err = new ApiError(status, "Invalid OTP");

        return ResponseEntity.status(status).body(err);
    }
}
