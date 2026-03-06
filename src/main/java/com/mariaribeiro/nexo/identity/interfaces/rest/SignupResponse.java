package com.mariaribeiro.nexo.identity.interfaces.rest;

import java.time.Instant;

public record SignupResponse(String accessToken, Instant expiresAt) {
}
