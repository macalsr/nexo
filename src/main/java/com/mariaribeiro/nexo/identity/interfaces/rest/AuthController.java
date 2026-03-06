package com.mariaribeiro.nexo.identity.interfaces.rest;

import com.mariaribeiro.nexo.identity.application.usecase.ForgotPasswordCommand;
import com.mariaribeiro.nexo.identity.application.usecase.ForgotPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.LoginCommand;
import com.mariaribeiro.nexo.identity.application.usecase.LoginResult;
import com.mariaribeiro.nexo.identity.application.usecase.LoginUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.SignupCommand;
import com.mariaribeiro.nexo.identity.application.usecase.SignupResult;
import com.mariaribeiro.nexo.identity.application.usecase.SignupUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;

    public AuthController(
            LoginUseCase loginUseCase,
            SignupUseCase signupUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase) {
        this.loginUseCase = loginUseCase;
        this.signupUseCase = signupUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(new LoginResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = signupUseCase.signup(new SignupCommand(request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignupResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.requestReset(new ForgotPasswordCommand(request.email()));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthMessageResponse("Check your email"));
    }
}
