package com.mariaribeiro.nexo.identity.adapters.in.rest;

import java.time.Instant;

public record AuthTokenResponse(String accessToken, Instant expiresAt) {
}
