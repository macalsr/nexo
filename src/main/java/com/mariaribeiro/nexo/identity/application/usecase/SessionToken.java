package com.mariaribeiro.nexo.identity.application.usecase;

import java.time.Instant;

public record SessionToken(String value, Instant expiresAt) {
}
