package com.mariaribeiro.nexo.application.auth;

import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserView(UUID id, String email, String passwordHash, Instant createdAt) {
}
