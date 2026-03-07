package com.mariaribeiro.nexo.identity.adapters.in.rest;

import java.time.Instant;

public record SignupResponse(String accessToken, Instant expiresAt) {
}
