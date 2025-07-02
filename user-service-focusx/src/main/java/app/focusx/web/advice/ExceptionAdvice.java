package app.focusx.web.advice;

import app.focusx.exception.PasswordUpdateException;
import app.focusx.exception.UsernameUpdateException;
import app.focusx.web.dto.ErrorResponse;
import app.focusx.web.dto.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(401)
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .error("Unauthorized")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler({UsernameUpdateException.class, PasswordUpdateException.class})
    public ResponseEntity<ErrorResponse> handleUserUpdateExceptions(Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .error("Bad Request")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue(),
                        error.getCode()))
                .toList();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Validation failed")
                        .path(request.getDescription(false))
                        .timestamp(LocalDateTime.now())
                        .fieldErrors(fieldErrors)
                        .build());
    }
}
