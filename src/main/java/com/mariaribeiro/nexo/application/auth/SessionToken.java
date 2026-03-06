package com.mariaribeiro.nexo.application.auth;

import java.time.Instant;

public record SessionToken(String value, Instant expiresAt) {
}
