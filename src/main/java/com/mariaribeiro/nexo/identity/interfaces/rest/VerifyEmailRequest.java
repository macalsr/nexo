package com.mariaribeiro.nexo.identity.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Verification token is required")
        String token) {
}
