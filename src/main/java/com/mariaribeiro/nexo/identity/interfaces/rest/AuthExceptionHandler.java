package com.mariaribeiro.nexo.identity.interfaces.rest;

import com.mariaribeiro.nexo.identity.application.usecase.DuplicateEmailException;
import com.mariaribeiro.nexo.identity.application.usecase.InvalidCredentialsException;
import com.mariaribeiro.nexo.identity.infrastructure.security.UnauthorizedException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("Invalid credentials"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<AuthErrorResponse> handleUnauthorized(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("Unauthorized"));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<AuthErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthErrorResponse("Unable to create account"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthValidationErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(fieldError -> errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthValidationErrorResponse("Validation failed", errors));
    }
}
