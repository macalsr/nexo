package com.mariaribeiro.nexo.identity.application.usecase;

import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserView(UUID id, String email, String passwordHash, Instant createdAt) {
}
