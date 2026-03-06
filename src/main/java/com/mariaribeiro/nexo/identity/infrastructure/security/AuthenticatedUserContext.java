package com.mariaribeiro.nexo.identity.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUserContext(UUID userId, String email) {
}
