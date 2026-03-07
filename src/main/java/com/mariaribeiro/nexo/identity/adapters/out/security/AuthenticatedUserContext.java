package com.mariaribeiro.nexo.identity.adapters.out.security;

import java.util.UUID;

public record AuthenticatedUserContext(UUID userId, String email) {
}
