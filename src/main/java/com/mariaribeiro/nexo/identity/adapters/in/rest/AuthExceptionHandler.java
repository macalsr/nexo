package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.usecase.DuplicateEmailException;
import com.mariaribeiro.nexo.identity.application.usecase.InvalidCredentialsException;
import com.mariaribeiro.nexo.identity.application.usecase.InvalidEmailVerificationTokenException;
import com.mariaribeiro.nexo.identity.application.usecase.InvalidRefreshTokenException;
import com.mariaribeiro.nexo.identity.application.usecase.InvalidResetTokenException;
import com.mariaribeiro.nexo.identity.adapters.out.security.UnauthorizedException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidResetToken(InvalidResetTokenException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthErrorResponse("Invalid reset token"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("Invalid refresh token"));
    }

    @ExceptionHandler(InvalidEmailVerificationTokenException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidEmailVerificationToken(
            InvalidEmailVerificationTokenException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthErrorResponse("Invalid verification token"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthValidationErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(fieldError -> errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthValidationErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<AuthValidationErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(exception.getParameterName(), missingParameterMessage(exception.getParameterName()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthValidationErrorResponse("Validation failed", errors));
    }

    private String missingParameterMessage(String parameterName) {
        if ("token".equals(parameterName)) {
            return "Verification token is required";
        }

        if (parameterName == null || parameterName.isBlank()) {
            return "Field is required";
        }

        return Character.toUpperCase(parameterName.charAt(0)) + parameterName.substring(1) + " is required";
    }
}
