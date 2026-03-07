package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.recovery.ForgotPasswordCommand;
import com.mariaribeiro.nexo.identity.application.recovery.ForgotPasswordService;
import com.mariaribeiro.nexo.identity.application.recovery.ResetPasswordCommand;
import com.mariaribeiro.nexo.identity.application.recovery.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRecoveryController {

    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordService.requestReset(new ForgotPasswordCommand(request.email()));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthMessageResponse("Check your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordService.resetPassword(new ResetPasswordCommand(request.token(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }
}
