package com.mariaribeiro.nexo.identity.interfaces.rest;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiresAt) {
}
