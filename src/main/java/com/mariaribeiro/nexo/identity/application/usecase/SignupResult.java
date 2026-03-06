package com.mariaribeiro.nexo.identity.application.usecase;

import java.time.Instant;

public record SignupResult(String accessToken, Instant expiresAt) {
}
