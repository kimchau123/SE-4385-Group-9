package com.group9.skillshub.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ResourceConflictException exception, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    // Returns 400 Bad Request when a value in the URL can't be converted to the expected type
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String message = "Invalid value '" + exception.getValue()
                + "' for parameter '" + exception.getName() + "'";
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleBadRequest(
            Exception exception, HttpServletRequest request) {
        String message = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "))
                : exception instanceof HttpMessageNotReadableException
                ? "Request body is missing or malformed"
                : exception.getMessage();
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
