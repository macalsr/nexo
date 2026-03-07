package com.mariaribeiro.nexo.identity.application.auth;

import java.time.Instant;

public record SessionToken(String value, Instant expiresAt) {
}

