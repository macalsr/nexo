package com.mariaribeiro.nexo.identity.adapters.in.rest;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiresAt) {
}
