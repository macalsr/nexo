package com.mariaribeiro.nexo.identity.adapters.in.security;

import java.util.UUID;

public record AuthenticatedUserContext(UUID userId, String email) {
}

