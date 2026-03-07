package com.mariaribeiro.nexo.identity.application.auth;

import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserView(
        UUID id,
        String email,
        String passwordHash,
        boolean emailVerified,
        Instant emailVerifiedAt,
        Instant createdAt) {
}

